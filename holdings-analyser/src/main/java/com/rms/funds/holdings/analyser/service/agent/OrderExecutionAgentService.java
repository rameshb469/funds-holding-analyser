package com.rms.funds.holdings.analyser.service.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.config.AgentConfigProperties;
import com.rms.funds.holdings.analyser.config.KiteConfigProperties;
import com.rms.funds.holdings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentCycleDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentPositionDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentStatusDto;
import com.rms.funds.holdings.analyser.entity.NseHolidayEntity;
import com.rms.funds.holdings.analyser.entity.OrderAgentCycleEntity;
import com.rms.funds.holdings.analyser.entity.OrderAgentPositionEntity;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.model.HoldingChangeMetricFilter;
import com.rms.funds.holdings.analyser.model.MarketCapCategoryType;
import com.rms.funds.holdings.analyser.repository.NseHolidayRepository;
import com.rms.funds.holdings.analyser.repository.OrderAgentCycleRepository;
import com.rms.funds.holdings.analyser.repository.OrderAgentPositionRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.StockHoldingService;
import com.rms.funds.holdings.analyser.service.kite.KiteException;
import com.rms.funds.holdings.analyser.service.kite.KiteService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Order-execution agent. On every cycle (1st-anchored, runs on the next trading day
 * after the 10th of each month), picks five LARGE_CAP stocks from
 * {@code GET /api/stock-holdings/metrics}, places CNC BUY orders for each, immediately
 * arms a SL-M at {@code entry * (1 - stopLossPct)} as the floor, then starts a 1-second
 * monitor daemon that ratchets the stop up the ladder as price climbs past
 * +5% / +10% / +15%, partial-exits one third of the position at each rung, and
 * liquidates anything still below the +5% floor at 15:10 IST.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionAgentService {

    public static final String CYCLE_PENDING = "PENDING";
    public static final String CYCLE_EXECUTED = "EXECUTED";
    public static final String CYCLE_FAILED = "FAILED";
    public static final String CYCLE_SKIPPED_HOLIDAY = "SKIPPED_HOLIDAY";
    public static final String CYCLE_SKIPPED_WEEKEND = "SKIPPED_WEEKEND";
    public static final String CYCLE_SKIPPED_NOT_TODAY = "SKIPPED_NOT_TODAY";

    public static final String POS_OPEN = "OPEN";
    public static final String POS_PARTIAL = "PARTIAL";
    public static final String POS_CLOSED_TRAIL = "CLOSED_TRAIL";
    public static final String POS_CLOSED_STOP = "CLOSED_STOP";
    public static final String POS_CLOSED_EOD = "CLOSED_EOD";
    public static final String POS_CLOSED_FLOOR = "CLOSED_FLOOR";
    public static final String POS_ERROR = "ERROR";
    public static final String POS_PAUSED = "PAUSED_SESSION_LOST";

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final List<String> OPEN_STATUSES = List.of(POS_OPEN, POS_PARTIAL);
    private static final int EOD_LIQUIDATE_HOUR = 15;
    private static final int EOD_LIQUIDATE_MINUTE = 10;

    private final KiteService kiteService;
    private final StockHoldingService stockHoldingService;
    private final StockInfoRepository stockInfoRepository;
    private final NseHolidayRepository holidayRepository;
    private final OrderAgentCycleRepository cycleRepository;
    private final OrderAgentPositionRepository positionRepository;
    private final AgentConfigProperties agentProps;
    private final KiteConfigProperties kiteProps;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicBoolean monitorRunning = new AtomicBoolean(false);
    private volatile Thread monitorThread;
    private final CopyOnWriteArrayList<String> monitorLogTail = new CopyOnWriteArrayList<>();

    @PostConstruct
    void announce() {
        if (agentProps.isEnabled()) {
            log.warn("OrderExecutionAgentService ENABLED. kite.sandbox={} — orders will hit {}",
                    kiteProps.isSandbox() ? "SANDBOX (safe)" : "ZERODHA LIVE",
                    kiteProps.isSandbox() ? "sandbox" : "your connected Zerodha account");
        } else {
            log.info("OrderExecutionAgentService disabled (agent.enabled=false)");
        }
    }

    @PreDestroy
    void shutdown() {
        stopMonitor();
    }

    // -----------------------------------------------------------------------
    //  Public control-plane API (consumed by BrokerController + scheduler)
    // -----------------------------------------------------------------------

    public OrderAgentStatusDto status() {
        LocalDate todayIst = LocalDate.now(IST);
        Set<LocalDate> holidays = loadHolidaySet();
        LocalDate nextEligible = decideNextEligibleDate(todayIst, holidays);
        OrderAgentCycleEntity lastCycle = cycleRepository.findTop20ByOrderByCreatedAtDesc()
                .stream().findFirst().orElse(null);
        long openPositions = positionRepository.findByStatusIn(OPEN_STATUSES).size();
        return OrderAgentStatusDto.builder()
                .running(monitorRunning.get())
                .enabled(agentProps.isEnabled())
                .liveMode(!kiteProps.isSandbox())
                .nextEligibleDate(nextEligible)
                .todayIst(todayIst)
                .lastCycle(lastCycle == null ? null : toCycleDto(lastCycle))
                .openPositions(openPositions)
                .todayRealisedPnl(sumTodayPnl(todayIst))
                .build();
    }

    public OrderAgentCycleDto triggerNow() {
        LocalDate todayIst = LocalDate.now(IST);
        Set<LocalDate> holidays = loadHolidaySet();
        LocalDate nextEligible = decideNextEligibleDate(todayIst, holidays);
        if (nextEligible == null || nextEligible.isAfter(todayIst)) {
            throw new IllegalArgumentException(
                    "Today (" + todayIst + ") is not an eligible cycle day. Next eligible: "
                            + nextEligible);
        }
        if (!agentProps.isEnabled()) {
            throw new IllegalStateException("agent.enabled=false — refusing to run a live cycle");
        }
        return runCycleForDate(todayIst, holidays);
    }

    public List<String> stopMonitor() {
        monitorRunning.set(false);
        Thread t = monitorThread;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        return List.copyOf(monitorLogTail);
    }

    public void dailyTick() {
        if (!agentProps.isEnabled()) {
            return;
        }
        LocalDate todayIst = LocalDate.now(IST);
        Set<LocalDate> holidays = loadHolidaySet();
        LocalDate nextEligible = decideNextEligibleDate(todayIst, holidays);
        if (nextEligible == null) {
            log.debug("No future cycle scheduled (anchor window exhausted).");
            return;
        }
        if (nextEligible.isAfter(todayIst)) {
            log.debug("Next eligible cycle day is {}, not today ({}).", nextEligible, todayIst);
            return;
        }
        try {
            runCycleForDate(todayIst, holidays);
        } catch (RuntimeException ex) {
            log.error("Order agent cycle failed for {}: {}", todayIst, ex.getMessage(), ex);
        }
    }

    // -----------------------------------------------------------------------
    //  Cycle scheduling (1st-of-month anchor)
    // -----------------------------------------------------------------------

    /**
     * Returns the next eligible trading date on or after {@code today}, given a 1st-anchored
     * 10-day rule. Returns null if the current 1st-anchored window has already passed and
     * we are still before the next 1st.
     */
    LocalDate decideNextEligibleDate(LocalDate today, Set<LocalDate> holidays) {
        LocalDate anchor = today.withDayOfMonth(1);
        LocalDate target = anchor.plusDays(9); // the 10th of the month
        if (target.isBefore(today)) {
            LocalDate nextAnchor = anchor.plusMonths(1);
            LocalDate nextTarget = nextAnchor.plusDays(9);
            return firstTradingDayOnOrAfter(nextTarget, holidays);
        }
        return firstTradingDayOnOrAfter(target, holidays);
    }

    private LocalDate firstTradingDayOnOrAfter(LocalDate date, Set<LocalDate> holidays) {
        LocalDate cursor = date;
        for (int i = 0; i < 14; i++) { // NSE never strings more than ~5 holidays together
            DayOfWeek dow = cursor.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !holidays.contains(cursor)) {
                return cursor;
            }
            cursor = cursor.plusDays(1);
        }
        return null;
    }

    // -----------------------------------------------------------------------
    //  Cycle execution
    // -----------------------------------------------------------------------

    @Transactional
    OrderAgentCycleDto runCycleForDate(LocalDate todayIst, Set<LocalDate> holidays) {
        LocalDate anchor = todayIst.withDayOfMonth(1);
        LocalDate target = anchor.plusDays(9);

        Optional<OrderAgentCycleEntity> existing = cycleRepository.findByCycleAnchorDate(anchor);
        if (existing.isPresent() && !List.of(CYCLE_FAILED, CYCLE_SKIPPED_HOLIDAY, CYCLE_SKIPPED_WEEKEND)
                .contains(existing.get().getStatus())) {
            log.info("Cycle for anchor {} already exists (status={}); skipping.", anchor, existing.get().getStatus());
            return toCycleDto(existing.get());
        }

        OrderAgentCycleEntity cycle = OrderAgentCycleEntity.builder()
                .cycleAnchorDate(anchor)
                .targetTradeDate(target)
                .actualTradeDate(todayIst)
                .status(CYCLE_PENDING)
                .build();
        cycle = cycleRepository.save(cycle);

        try {
            List<HoldingChangeMetricDto.StockPerformanceChange> picks = pickFiveLargeCaps();
            if (picks.isEmpty()) {
                throw new IllegalStateException("No LARGE_CAP picks available from /api/stock-holdings/metrics");
            }
            cycle.setPicksJson(objectMapper.writeValueAsString(toPickIdList(picks)));
            cycleRepository.save(cycle);

            List<String> orderIds = new ArrayList<>();
            List<OrderAgentPositionEntity> opened = new ArrayList<>();
            for (HoldingChangeMetricDto.StockPerformanceChange pick : picks) {
                Optional<OrderAgentPositionEntity> openedPos =
                        openPosition(cycle.getId(), pick);
                if (openedPos != null && openedPos.isPresent()) {
                    opened.add(openedPos.get());
                    if (openedPos.get().getEntryOrderId() != null) {
                        orderIds.add(openedPos.get().getEntryOrderId());
                    }
                }
            }
            cycle.setBrokerOrderIds(String.join(",", orderIds));
            cycle.setStatus(CYCLE_EXECUTED);
            cycleRepository.save(cycle);

            if (!opened.isEmpty()) {
                startMonitor();
            }
            return toCycleDto(cycle);
        } catch (Exception ex) {
            log.error("Cycle execution failed: {}", ex.getMessage(), ex);
            cycle.setStatus(CYCLE_FAILED);
            cycle.setSkipReason(truncate(ex.getMessage(), 256));
            cycleRepository.save(cycle);
            return toCycleDto(cycle);
        }
    }

    List<HoldingChangeMetricDto.StockPerformanceChange> pickFiveLargeCaps() {
        HoldingChangeMetricDto snapshot = stockHoldingService.getHoldingChangeMetrics(
                HoldingChangeMetricFilter.builder()
                        .marketCapCategory(MarketCapCategoryType.LARGE_CAP)
                        .build());
        if (snapshot == null || snapshot.getStockPerformance() == null) {
            return Collections.emptyList();
        }
        Map<String, List<HoldingChangeMetricDto.StockPerformanceChange>> groups = snapshot.getStockPerformance();

        List<HoldingChangeMetricDto.StockPerformanceChange> candidates = new ArrayList<>();
        for (String key : List.of("top10Overall", "top10ByExposure", "top10ByRelativePct", "top10ByFundCount")) {
            List<HoldingChangeMetricDto.StockPerformanceChange> bucket = groups.get(key);
            if (bucket == null) continue;
            for (HoldingChangeMetricDto.StockPerformanceChange c : bucket) {
                MarketCapCategoryType cat = MarketCapCategoryType.fromIdLenient(c.getMarketCapCategory());
                if (cat == null || !cat.isLargeCap()) {
                    continue;
                }
                if (candidates.stream().noneMatch(p -> p.getId().equals(c.getId()))) {
                    candidates.add(c);
                }
                if (candidates.size() >= agentProps.getPicksPerCycle()) break;
            }
            if (candidates.size() >= agentProps.getPicksPerCycle()) break;
        }

        List<HoldingChangeMetricDto.StockPerformanceChange> top = candidates.stream()
                .limit(agentProps.getPicksPerCycle())
                .collect(Collectors.toList());
        log.info("Agent picked {} large-cap stocks: {}",
                top.size(),
                top.stream().map(HoldingChangeMetricDto.StockPerformanceChange::getSymbol).toList());
        return top;
    }

    private List<Long> toPickIdList(List<HoldingChangeMetricDto.StockPerformanceChange> picks) {
        return picks.stream().map(HoldingChangeMetricDto.StockPerformanceChange::getId).collect(Collectors.toList());
    }

    @Transactional
    Optional<OrderAgentPositionEntity> openPosition(Long cycleId, HoldingChangeMetricDto.StockPerformanceChange pick) {
        StockInfoEntity stock = stockInfoRepository.findById(pick.getId()).orElse(null);
        if (stock == null) {
            log.warn("Pick {} resolved but no stock_details row found.", pick.getId());
            return Optional.empty();
        }

        KiteOrderResponseDto entryResp = placeBuy(stock);
        if (entryResp == null || entryResp.getKiteOrderId() == null) {
            return Optional.empty();
        }
        Double fillPrice = resolveFillPrice(entryResp.getKiteOrderId());
        if (fillPrice == null) {
            log.warn("Entry for {} returned no fill price within 5s; marking position ERROR", stock.getSymbol());
            return persistPosition(cycleId, stock, 0, 0.0, entryResp.getKiteOrderId(), null, POS_ERROR);
        }
        int qty = computeQuantity(fillPrice);
        if (qty <= 0) {
            log.warn("Computed qty for {} at price {} is 0; skipping", stock.getSymbol(), fillPrice);
            return Optional.empty();
        }

        OrderAgentPositionEntity pos = persistPosition(cycleId, stock, qty, fillPrice,
                entryResp.getKiteOrderId(), null, POS_OPEN).orElse(null);
        if (pos == null) {
            return Optional.empty();
        }

        KiteOrderResponseDto slResp = placeStopLoss(stock, qty, stopPrice(fillPrice));
        if (slResp != null && slResp.getKiteOrderId() != null) {
            pos.setStopLossOrderId(slResp.getKiteOrderId());
            positionRepository.save(pos);
        }
        return Optional.of(pos);
    }

    private Optional<OrderAgentPositionEntity> persistPosition(Long cycleId, StockInfoEntity stock,
                                                               int qty, double fillPrice,
                                                               String entryOrderId, String slOrderId,
                                                               String status) {
        OrderAgentPositionEntity pos = OrderAgentPositionEntity.builder()
                .cycleId(cycleId)
                .stockId(stock.getId())
                .symbol(stock.getSymbol())
                .exchange("NSE")
                .qty(qty)
                .entryPrice(fillPrice)
                .entryOrderId(entryOrderId)
                .stopLossOrderId(slOrderId)
                .trailHigh(fillPrice)
                .ladderLevel((short) 0)
                .status(status)
                .build();
        return Optional.of(positionRepository.save(pos));
    }

    private KiteOrderResponseDto placeBuy(StockInfoEntity stock) {
        KiteOrderRequestDto req = KiteOrderRequestDto.builder()
                .stockId(stock.getId())
                .exchange("NSE")
                .transactionType("BUY")
                .orderType("MARKET")
                .product("CNC")
                .validity("DAY")
                .tag("agent-v1-entry")
                .build();
        try {
            return kiteService.placeOrder(req);
        } catch (RuntimeException ex) {
            log.error("BUY failed for {}: {}", stock.getSymbol(), ex.getMessage());
            return null;
        }
    }

    private KiteOrderResponseDto placeStopLoss(StockInfoEntity stock, int qty, double triggerPrice) {
        KiteOrderRequestDto req = KiteOrderRequestDto.builder()
                .stockId(stock.getId())
                .exchange("NSE")
                .transactionType("SELL")
                .orderType("SL-M")
                .product("CNC")
                .quantity(qty)
                .triggerPrice(round2(triggerPrice))
                .validity("DAY")
                .tag("agent-v1-sl")
                .build();
        try {
            return kiteService.placeOrder(req);
        } catch (RuntimeException ex) {
            log.error("SL-M placement failed for {}: {}", stock.getSymbol(), ex.getMessage());
            return null;
        }
    }

    private Double resolveFillPrice(String kiteOrderId) {
        long deadline = System.currentTimeMillis() + 5_000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Double fill = readPriceFromOrderBook(kiteOrderId);
                if (fill != null && fill > 0) {
                    return fill;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private Double readPriceFromOrderBook(String kiteOrderId) {
        return kiteService.getOrderBook().stream()
                .filter(o -> kiteOrderId.equals(o.getOrderId()))
                .filter(o -> "COMPLETE".equalsIgnoreCase(o.getStatus()))
                .map(com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto::getPrice)
                .filter(p -> p != null && p > 0)
                .findFirst()
                .orElse(null);
    }

    private int computeQuantity(double fillPrice) {
        double perStock = agentProps.getCapitalPerCycle() / Math.max(1, agentProps.getPicksPerCycle());
        int qty = (int) Math.floor(perStock / fillPrice);
        return Math.max(qty, 0);
    }

    private double stopPrice(double entryPrice) {
        return round2(entryPrice * (1.0 - agentProps.getStopLossPct()));
    }

    // -----------------------------------------------------------------------
    //  Monitor loop (1-second cadence)
    // -----------------------------------------------------------------------

    void startMonitor() {
        if (monitorRunning.compareAndSet(false, true)) {
            monitorThread = new Thread(this::monitorLoop, "order-agent-monitor");
            monitorThread.setDaemon(true);
            monitorThread.start();
        }
    }

    private void monitorLoop() {
        log.info("Monitor daemon started");
        while (monitorRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                tick();
            } catch (KiteException ke) {
                if (ke.isSessionExpired()) {
                    parkPositionsOnSessionLoss();
                    monitorLog("session expired — pausing monitor until /api/broker/callback is called again");
                    break;
                }
                monitorLog("kite error: " + ke.getMessage());
            } catch (Exception ex) {
                monitorLog("monitor error: " + ex.getMessage());
                log.error("Monitor tick error", ex);
            }
            try {
                Thread.sleep(agentProps.getMonitorIntervalMs());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        monitorRunning.set(false);
        log.info("Monitor daemon stopped");
    }

    void tick() {
        List<OrderAgentPositionEntity> open = positionRepository.findByStatusIn(OPEN_STATUSES);
        if (open.isEmpty()) {
            monitorRunning.set(false);
            return;
        }
        List<String> keys = open.stream()
                .map(p -> p.getExchange() + ":" + p.getSymbol())
                .toList();
        Map<String, Double> ltpBySymbol = fetchLtps(keys);
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime istNow = ZonedDateTime.now(IST);

        for (OrderAgentPositionEntity pos : open) {
            Double ltp = ltpBySymbol.get(pos.getExchange() + ":" + pos.getSymbol());
            if (ltp == null || ltp <= 0) {
                continue;
            }
            if (ltp > pos.getTrailHigh()) {
                pos.setTrailHigh(ltp);
            }
            short level = pos.getLadderLevel() == null ? 0 : pos.getLadderLevel();
            short newLevel = level;
            if (ltp >= rungPrice(pos.getEntryPrice(), agentProps.getLadderLevel3Pct())) {
                newLevel = (short) 3;
            } else if (ltp >= rungPrice(pos.getEntryPrice(), agentProps.getLadderLevel2Pct())) {
                newLevel = (short) 2;
            } else if (ltp >= rungPrice(pos.getEntryPrice(), agentProps.getLadderLevel1Pct())) {
                newLevel = (short) 1;
            }
            if (newLevel != level) {
                pos.setLadderLevel(newLevel);
                partialExit(pos, newLevel, ltp);
                ratchetStopLoss(pos, ltp);
            }
            if (agentProps.isEodLiquidateBelowFloor()
                    && istNow.getHour() == EOD_LIQUIDATE_HOUR
                    && istNow.getMinute() >= EOD_LIQUIDATE_MINUTE
                    && ltp < rungPrice(pos.getEntryPrice(), agentProps.getLadderLevel1Pct())) {
                forceExit(pos, ltp, POS_CLOSED_EOD);
            }
            pos.setUpdatedAt(now);
            positionRepository.save(pos);
        }

        if (positionRepository.findByStatusIn(OPEN_STATUSES).isEmpty()) {
            closeCycleIfAllDone();
            monitorRunning.set(false);
        }
    }

    private Map<String, Double> fetchLtps(List<String> keys) {
        Map<String, Double> out = new HashMap<>();
        try {
            List<com.rms.funds.holdings.analyser.service.kite.KiteModels.KiteQuoteEnvelope> envelopes =
                    kiteService.getQuotes(keys);
            for (int i = 0; i < keys.size() && i < envelopes.size(); i++) {
                com.rms.funds.holdings.analyser.service.kite.KiteModels.KiteQuote q = envelopes.get(i).getQuote();
                if (q != null && q.getLastPrice() != null && q.getLastPrice() > 0) {
                    out.put(keys.get(i), q.getLastPrice());
                }
            }
        } catch (Exception ignored) {
        }
        // Per-key fallback to single-instrument quote when the batch call misses a row
        // (e.g. one symbol returned a paid-data error envelope).
        for (String key : keys) {
            if (out.containsKey(key)) continue;
            String[] parts = key.split(":", 2);
            try {
                Double ltp = kiteService.getQuote(parts[0], parts[1]).getQuote().getLastPrice();
                if (ltp != null && ltp > 0) {
                    out.put(key, ltp);
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void partialExit(OrderAgentPositionEntity pos, short newLevel, double ltp) {
        int exitQty = (int) Math.floor(pos.getQty() * agentProps.getLadderExitFraction());
        if (exitQty <= 0) {
            return;
        }
        KiteOrderRequestDto req = KiteOrderRequestDto.builder()
                .stockId(pos.getStockId())
                .exchange(pos.getExchange())
                .transactionType("SELL")
                .orderType("MARKET")
                .product("CNC")
                .quantity(exitQty)
                .validity("DAY")
                .tag("agent-v1-rung-" + newLevel)
                .build();
        try {
            KiteOrderResponseDto resp = kiteService.placeOrder(req);
            if (resp != null && resp.getKiteOrderId() != null) {
                String existing = pos.getLadderExitOrderIds();
                String joined = (existing == null || existing.isBlank())
                        ? resp.getKiteOrderId()
                        : existing + "," + resp.getKiteOrderId();
                pos.setLadderExitOrderIds(joined);
                pos.setStatus(POS_PARTIAL);
            }
        } catch (Exception ex) {
            monitorLog("partial exit failed for " + pos.getSymbol() + ": " + ex.getMessage());
        }
    }

    private void ratchetStopLoss(OrderAgentPositionEntity pos, double ltp) {
        double newStop = round2(ltp * (1.0 - agentProps.getStopLossPct()));
        if (pos.getStopLossOrderId() != null) {
            try {
                kiteService.cancelOrder(pos.getStopLossOrderId());
            } catch (Exception ex) {
                monitorLog("cancel SL failed for " + pos.getSymbol() + ": " + ex.getMessage());
            }
        }
        StockInfoEntity stock = stockInfoRepository.findById(pos.getStockId()).orElse(null);
        if (stock == null) return;
        KiteOrderResponseDto resp = placeStopLoss(stock, pos.getQty(), newStop);
        if (resp != null && resp.getKiteOrderId() != null) {
            pos.setStopLossOrderId(resp.getKiteOrderId());
        }
    }

    private void forceExit(OrderAgentPositionEntity pos, double ltp, String closeReason) {
        if (pos.getStopLossOrderId() != null) {
            try {
                kiteService.cancelOrder(pos.getStopLossOrderId());
            } catch (Exception ignored) {
            }
        }
        KiteOrderRequestDto req = KiteOrderRequestDto.builder()
                .stockId(pos.getStockId())
                .exchange(pos.getExchange())
                .transactionType("SELL")
                .orderType("MARKET")
                .product("CNC")
                .quantity(pos.getQty())
                .validity("DAY")
                .tag("agent-v1-" + closeReason.toLowerCase())
                .build();
        try {
            KiteOrderResponseDto resp = kiteService.placeOrder(req);
            if (resp != null && resp.getKiteOrderId() != null) {
                pos.setStatus(closeReason);
                pos.setClosePrice(ltp);
                pos.setRealisedPnl((ltp - pos.getEntryPrice()) * pos.getQty());
                pos.setClosedAt(LocalDateTime.now());
                positionRepository.save(pos);
            }
        } catch (Exception ex) {
            monitorLog("force exit failed for " + pos.getSymbol() + ": " + ex.getMessage());
        }
    }

    private void parkPositionsOnSessionLoss() {
        for (OrderAgentPositionEntity p : positionRepository.findByStatusIn(OPEN_STATUSES)) {
            p.setStatus(POS_PAUSED);
            positionRepository.save(p);
        }
    }

    private void closeCycleIfAllDone() {
        List<OrderAgentPositionEntity> remaining = positionRepository.findByStatusIn(OPEN_STATUSES);
        if (!remaining.isEmpty()) {
            return;
        }
        List<OrderAgentCycleEntity> openCycles = cycleRepository
                .findByStatusInOrderByCreatedAtDesc(List.of(CYCLE_EXECUTED));
        for (OrderAgentCycleEntity cycle : openCycles) {
            List<OrderAgentPositionEntity> positions = positionRepository.findByCycleId(cycle.getId());
            if (positions.isEmpty()) continue;
            boolean allClosed = positions.stream().allMatch(p ->
                    List.of(POS_CLOSED_TRAIL, POS_CLOSED_STOP, POS_CLOSED_EOD,
                            POS_CLOSED_FLOOR, POS_ERROR).contains(p.getStatus()));
            if (!allClosed) continue;
            double pnl = positions.stream()
                    .filter(p -> p.getRealisedPnl() != null)
                    .mapToDouble(OrderAgentPositionEntity::getRealisedPnl)
                    .sum();
            cycle.setClosedAt(LocalDateTime.now());
            cycle.setRealisedPnl(pnl);
            cycleRepository.save(cycle);
        }
    }

    private double rungPrice(double entry, double pct) {
        return entry * (1.0 + pct);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private void monitorLog(String line) {
        String stamped = LocalDateTime.now(IST) + " " + line;
        monitorLogTail.add(0, stamped);
        while (monitorLogTail.size() > 50) {
            monitorLogTail.remove(monitorLogTail.size() - 1);
        }
        log.info(line);
    }

    // -----------------------------------------------------------------------
    //  Holidays + audit lookups
    // -----------------------------------------------------------------------

    private Set<LocalDate> loadHolidaySet() {
        return holidayRepository.findAllByOrderByHolidayDateAsc().stream()
                .map(NseHolidayEntity::getHolidayDate)
                .collect(Collectors.toSet());
    }

    private double sumTodayPnl(LocalDate today) {
        return cycleRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .filter(c -> c.getRealisedPnl() != null)
                .filter(c -> c.getClosedAt() != null && c.getClosedAt().toLocalDate().equals(today))
                .mapToDouble(OrderAgentCycleEntity::getRealisedPnl)
                .sum();
    }

    // -----------------------------------------------------------------------
    //  DTO mapping
    // -----------------------------------------------------------------------

    public OrderAgentCycleDto toCycleDto(OrderAgentCycleEntity c) {
        List<String> picks = readPicks(c.getPicksJson());
        List<String> ids = c.getBrokerOrderIds() == null
                ? List.of()
                : Arrays.stream(c.getBrokerOrderIds().split(",")).filter(s -> !s.isBlank()).toList();
        return OrderAgentCycleDto.builder()
                .id(c.getId())
                .cycleAnchorDate(c.getCycleAnchorDate())
                .targetTradeDate(c.getTargetTradeDate())
                .actualTradeDate(c.getActualTradeDate())
                .status(c.getStatus())
                .skipReason(c.getSkipReason())
                .picks(picks)
                .brokerOrderIds(ids)
                .closedAt(c.getClosedAt())
                .realisedPnl(c.getRealisedPnl())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public OrderAgentPositionDto toPositionDto(OrderAgentPositionEntity p) {
        return OrderAgentPositionDto.builder()
                .id(p.getId())
                .cycleId(p.getCycleId())
                .stockId(p.getStockId())
                .symbol(p.getSymbol())
                .exchange(p.getExchange())
                .qty(p.getQty())
                .entryPrice(p.getEntryPrice())
                .trailHigh(p.getTrailHigh())
                .ladderLevel(p.getLadderLevel() == null ? Integer.valueOf(0) : Integer.valueOf(p.getLadderLevel()))
                .stopLossOrderId(p.getStopLossOrderId())
                .status(p.getStatus())
                .closePrice(p.getClosePrice())
                .realisedPnl(p.getRealisedPnl())
                .closedAt(p.getClosedAt())
                .build();
    }

    private List<String> readPicks(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Long> ids = objectMapper.readValue(json, new TypeReference<>() {});
            return ids.stream().map(String::valueOf).toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
