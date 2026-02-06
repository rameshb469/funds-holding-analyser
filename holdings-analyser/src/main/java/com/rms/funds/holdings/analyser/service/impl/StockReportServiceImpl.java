package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.dto.BacktestEntryDto;
import com.rms.funds.holdings.analyser.dto.StockVolumeReportDto;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockReportServiceImpl {
    private final StockPriceHistoryRepository repo;
    private final StockInfoRepository stockInfoRepository;
    private static final double DEFAULT_TRADE_COST_PCT_PER_SIDE = 0.001; // 0.1%

    public StockReportServiceImpl(StockPriceHistoryRepository repo, StockInfoRepository stockInfoRepository) {
        this.repo = repo;
        this.stockInfoRepository = stockInfoRepository;
    }

    public List<StockVolumeReportDto> generateVolumeReport(LocalDate start, LocalDate end) {
        List<StockPriceHistory> rows = repo.findByTradeDateBetween(start, end);
        Map<String, List<StockPriceHistory>> groups = rows.stream()
                .collect(Collectors.groupingBy(r -> {
                    if (r.getStock() != null && r.getStock().getId() != null) return "ID:" + r.getStock().getId();
                    if (r.getTickerSymbol() != null) return "T:" + r.getTickerSymbol();
                    return "T:UNKNOWN_" + UUID.randomUUID();
                }));

        List<StockVolumeReportDto> out = new ArrayList<>();
        for (Map.Entry<String, List<StockPriceHistory>> e : groups.entrySet()) {
            List<StockPriceHistory> list = e.getValue();
            String key = e.getKey();
            Long stockId = key.startsWith("ID:") ? tryParseLong(key.substring(3)) : null;

            String ticker = list.stream().map(StockPriceHistory::getTickerSymbol).filter(Objects::nonNull).findFirst()
                    .orElseGet(() -> list.stream().map(StockPriceHistory::getStock).filter(Objects::nonNull)
                            .map(StockInfoEntity::getSymbol).filter(Objects::nonNull).findFirst().orElse(null));

            List<Long> vols = list.stream().map(StockPriceHistory::getTotalTradingVolume).filter(Objects::nonNull).toList();
            double avgVol, stdVol = 0d;
            if (!vols.isEmpty()) {
                double sum = vols.stream().mapToDouble(Long::doubleValue).sum();
                avgVol = sum / vols.size();
                double var = vols.stream().mapToDouble(v -> (v - avgVol) * (v - avgVol)).sum() / vols.size();
                stdVol = Math.sqrt(var);
            } else {
                avgVol = 0d;
            }

            List<BigDecimal> turns = list.stream().map(StockPriceHistory::getTotalTradedValue).filter(Objects::nonNull).toList();
            BigDecimal avgTurn = BigDecimal.ZERO;
            if (!turns.isEmpty()) {
                BigDecimal sumTurn = turns.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                avgTurn = sumTurn.divide(BigDecimal.valueOf(turns.size()), 2, RoundingMode.HALF_UP);
            }

            BigDecimal lastClose = list.stream().filter(r -> r.getClosePrice() != null)
                    .max(Comparator.comparing(StockPriceHistory::getTradeDate)).map(StockPriceHistory::getClosePrice).orElse(null);

            if (lastClose == null && stockId != null) {
                StockPriceHistory fb = repo.findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(stockId, end.plusDays(1));
                if (fb != null) lastClose = fb.getClosePrice();
            }

            BigDecimal marketCap = list.stream().map(StockPriceHistory::getStock).filter(Objects::nonNull)
                    .map(StockInfoEntity::getMarketCap).filter(Objects::nonNull).map(BigDecimal::valueOf).findFirst().orElse(null);

            out.add(new StockVolumeReportDto(stockId, ticker, avgVol, stdVol, list.size(), avgTurn, lastClose, marketCap));
        }

        out.sort(Comparator.comparingDouble(r -> -score(r)));
        return out;
    }

    private static Long tryParseLong(String s) { try { return Long.parseLong(s); } catch (Exception ex) { return null; } }
    private double score(StockVolumeReportDto r) { return r.getVolumeStdDev() <= 0 ? r.getAvgVolume() : r.getAvgVolume() / r.getVolumeStdDev(); }

    public List<BacktestEntryDto> backtestTopN(LocalDate from, LocalDate to, int windowMonths, int topN) {
        return backtestTopN(from, to, windowMonths, topN, DEFAULT_TRADE_COST_PCT_PER_SIDE);
    }

    public List<BacktestEntryDto> backtestTopN(LocalDate from, LocalDate to, int windowMonths, int topN, double tradeCostPctPerSide) {
        List<BacktestEntryDto> results = new ArrayList<>();
        Set<Long> topUniverse = stockInfoRepository.findTop1000ByOrderByMarketCapDesc().stream().map(StockInfoEntity::getId).collect(Collectors.toSet());
        double roundTripCost = 1 - (1 - tradeCostPctPerSide) * (1 - tradeCostPctPerSide);

        LocalDate windowStart = from;
        while (!windowStart.plusMonths(windowMonths).isAfter(to)) {
            LocalDate windowEnd = windowStart.plusMonths(windowMonths).minusDays(1);
            LocalDate holdStart = windowEnd.plusDays(1);
            LocalDate holdEnd = windowEnd.plusMonths(1);

            List<StockVolumeReportDto> reports = generateVolumeReport(windowStart, windowEnd);
            List<StockVolumeReportDto> picks = reports.stream().filter(r -> {
                Long sid = r.getStockId(); if (sid != null) return topUniverse.contains(sid);
                if (r.getTicker() != null) return stockInfoRepository.findBySymbol(r.getTicker()).map(s -> topUniverse.contains(s.getId())).orElse(false);
                return false;
            }).sorted(Comparator.comparingDouble(r -> -score(r))).limit(topN).toList();

            if (picks.isEmpty()) { results.add(new BacktestEntryDto(windowStart, windowEnd, 0d, 0, 0)); windowStart = windowStart.plusMonths(1); continue; }

            List<StockPriceHistory> forwardRows = repo.findByTradeDateBetween(holdStart, holdEnd);
            Map<Long, BigDecimal> forwardById = forwardRows.stream().filter(r -> r.getStock() != null && r.getClosePrice() != null)
                    .collect(Collectors.groupingBy(r -> r.getStock().getId(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(StockPriceHistory::getTradeDate)), opt -> opt.map(StockPriceHistory::getClosePrice).orElse(BigDecimal.ZERO))));
            Map<String, BigDecimal> forwardByTicker = forwardRows.stream().filter(r -> r.getTickerSymbol() != null && r.getClosePrice() != null)
                    .collect(Collectors.groupingBy(StockPriceHistory::getTickerSymbol, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(StockPriceHistory::getTradeDate)), opt -> opt.map(StockPriceHistory::getClosePrice).orElse(BigDecimal.ZERO))));

            double sum = 0d; int count = 0;
            for (StockVolumeReportDto p : picks) {
                BigDecimal start = p.getLastClosePrice(); if (start == null || start.compareTo(BigDecimal.ZERO) == 0) continue;
                BigDecimal f = p.getStockId() != null ? forwardById.get(p.getStockId()) : null;
                if (f == null && p.getTicker() != null) f = forwardByTicker.get(p.getTicker());
                if (f == null || f.compareTo(BigDecimal.ZERO) == 0) continue;
                double gross = f.subtract(start).divide(start, 6, RoundingMode.HALF_UP).doubleValue();
                double net = (1 + gross) * (1 - roundTripCost) - 1;
                sum += net; count++;
            }

            double avg = count > 0 ? sum / count : 0d;
            results.add(new BacktestEntryDto(windowStart, windowEnd, avg, picks.size(), count));
            windowStart = windowStart.plusMonths(1);
        }
        return results;
    }

    public Map<String, Object> backtestPortfolio(LocalDate from, LocalDate to, int windowMonths, int topN, double tradeCostPctPerSide) {
        Map<String, Object> out = new HashMap<>();
        List<BacktestEntryDto> entries = backtestTopN(from, to, windowMonths, topN, tradeCostPctPerSide);
        out.put("entries", entries);
        double overallAvg = entries.stream().filter(e -> e.getPicksWithForwardData() > 0)
                .mapToDouble(BacktestEntryDto::getAvgForwardReturn).average().orElse(0d);
        out.put("overallAvgReturnPct", overallAvg);
        return out;
    }
}
