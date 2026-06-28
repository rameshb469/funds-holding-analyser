package com.rms.funds.holdings.analyser.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.config.AgentConfigProperties;
import com.rms.funds.holdings.analyser.config.KiteConfigProperties;
import com.rms.funds.holdings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.entity.OrderAgentCycleEntity;
import com.rms.funds.holdings.analyser.entity.OrderAgentPositionEntity;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.model.IdName;
import com.rms.funds.holdings.analyser.repository.NseHolidayRepository;
import com.rms.funds.holdings.analyser.repository.OrderAgentCycleRepository;
import com.rms.funds.holdings.analyser.repository.OrderAgentPositionRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.StockHoldingService;
import com.rms.funds.holdings.analyser.service.kite.KiteModels;
import com.rms.funds.holdings.analyser.service.kite.KiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExecutionAgentServiceTest {

    @Mock
    private KiteService kiteService;
    @Mock
    private StockHoldingService stockHoldingService;
    @Mock
    private StockInfoRepository stockInfoRepository;
    @Mock
    private NseHolidayRepository holidayRepository;
    @Mock
    private OrderAgentCycleRepository cycleRepository;
    @Mock
    private OrderAgentPositionRepository positionRepository;

    @Spy
    private AgentConfigProperties agentProps = new AgentConfigProperties();
    @Spy
    private KiteConfigProperties kiteProps = new KiteConfigProperties();

    @InjectMocks
    private OrderExecutionAgentService service;

    @BeforeEach
    void setUp() {
        kiteProps.setSandbox(true);
        agentProps.setEnabled(true);
        lenient().when(holidayRepository.findAllByOrderByHolidayDateAsc()).thenReturn(Collections.emptyList());
        lenient().when(cycleRepository.findByCycleAnchorDate(any())).thenReturn(Optional.empty());
        lenient().when(cycleRepository.save(any(OrderAgentCycleEntity.class)))
                .thenAnswer(inv -> {
                    OrderAgentCycleEntity e = inv.getArgument(0);
                    if (e.getId() == null) {
                        e.setId(1L);
                    }
                    return e;
                });
        lenient().when(positionRepository.save(any(OrderAgentPositionEntity.class)))
                .thenAnswer(inv -> {
                    OrderAgentPositionEntity e = inv.getArgument(0);
                    if (e.getId() == null) {
                        e.setId(100L);
                    }
                    return e;
                });
    }

    @Nested
    class DecideNextEligibleDate {

        @Test
        void tenthOnSaturdayRollsToMonday() {
            // 2026-10-10 is a Saturday → next trading day = Monday 2026-10-12.
            // today = 2026-10-01 → anchor = 2026-10-01 → target = 2026-10-10
            LocalDate result = service.decideNextEligibleDate(LocalDate.of(2026, 10, 1), Set.of());
            assertThat(result).isEqualTo(LocalDate.of(2026, 10, 12));
        }

        @Test
        void skipsNseHoliday() {
            // today = 2026-02-01 → anchor = 2026-02-01 → target = 2026-02-10.
            // Inject an artificial NSE holiday on 2026-02-10 and assert the agent rolls forward to 2026-02-11.
            LocalDate result = service.decideNextEligibleDate(
                    LocalDate.of(2026, 2, 1),
                    Set.of(LocalDate.of(2026, 2, 10))
            );
            assertThat(result).isEqualTo(LocalDate.of(2026, 2, 11));
        }

        @Test
        void pastTargetBeforeNextAnchorReturnsNextMonth() {
            // 2026-07-15: 1st-anchored July target was 2026-07-10 — passed. So answer is next 1st
            // anchor (Aug 1) + 9 = Aug 10 → Monday 2026-08-10.
            LocalDate result = service.decideNextEligibleDate(LocalDate.of(2026, 7, 15), Set.of());
            assertThat(result).isEqualTo(LocalDate.of(2026, 8, 10));
        }

        @Test
        void beforeTenthReturnsCurrentMonthTenth() {
            LocalDate result = service.decideNextEligibleDate(LocalDate.of(2026, 6, 5), Set.of());
            assertThat(result).isEqualTo(LocalDate.of(2026, 6, 10));
        }
    }

    @Nested
    class PickFiveLargeCaps {

        @Test
        void picksTopFiveLargeCaps() {
            HoldingChangeMetricDto snapshot = HoldingChangeMetricDto.builder()
                    .stockPerformance(Map.of("top10Overall", List.of(
                            pick(1L, "RELIANCE", "LARGE_CAP"),
                            pick(2L, "HDFCBANK", "LARGE_CAP"),
                            pick(3L, "INFY", "LARGE_CAP"),
                            pick(4L, "TCS", "LARGE_CAP"),
                            pick(5L, "ICICIBANK", "LARGE_CAP"),
                            pick(6L, "SBIN", "LARGE_CAP")
                    )))
                    .build();
            when(stockHoldingService.getHoldingChangeMetrics(any())).thenReturn(snapshot);

            List<HoldingChangeMetricDto.StockPerformanceChange> picks = service.pickFiveLargeCaps();

            assertThat(picks).hasSize(5);
            assertThat(picks).extracting(HoldingChangeMetricDto.StockPerformanceChange::getSymbol)
                    .containsExactly("RELIANCE", "HDFCBANK", "INFY", "TCS", "ICICIBANK");
        }

        @Test
        void excludesNonLargeCap() {
            HoldingChangeMetricDto snapshot = HoldingChangeMetricDto.builder()
                    .stockPerformance(Map.of("top10Overall", List.of(
                            pick(1L, "RELIANCE", "LARGE_CAP"),
                            pick(2L, "MIDSTOCK", "MID_CAP"),
                            pick(3L, "SMALLSTOCK", "SMALL_CAP"),
                            pick(4L, "MICROSTOCK", "MICRO_CAP")
                    )))
                    .build();
            when(stockHoldingService.getHoldingChangeMetrics(any())).thenReturn(snapshot);

            List<HoldingChangeMetricDto.StockPerformanceChange> picks = service.pickFiveLargeCaps();

            assertThat(picks).hasSize(1);
            assertThat(picks.get(0).getSymbol()).isEqualTo("RELIANCE");
        }

        @Test
        void emptyWhenNoLargeCap() {
            HoldingChangeMetricDto snapshot = HoldingChangeMetricDto.builder()
                    .stockPerformance(Map.of("top10Overall", List.of(
                            pick(1L, "MID", "MID_CAP")
                    )))
                    .build();
            when(stockHoldingService.getHoldingChangeMetrics(any())).thenReturn(snapshot);

            assertThat(service.pickFiveLargeCaps()).isEmpty();
        }

        @Test
        void toleratesLegacyMircoCapSpelling() {
            HoldingChangeMetricDto snapshot = HoldingChangeMetricDto.builder()
                    .stockPerformance(Map.of("top10Overall", List.of(
                            pick(1L, "OLD", "MIRCO_CAP")
                    )))
                    .build();
            when(stockHoldingService.getHoldingChangeMetrics(any())).thenReturn(snapshot);

            assertThat(service.pickFiveLargeCaps()).isEmpty();
        }

        @Test
        void fallsBackToExposureWhenOverallHasFewerThanFive() {
            HoldingChangeMetricDto snapshot = HoldingChangeMetricDto.builder()
                    .stockPerformance(Map.of(
                            "top10Overall", List.of(
                                    pick(1L, "RELIANCE", "LARGE_CAP"),
                                    pick(2L, "HDFCBANK", "LARGE_CAP")
                            ),
                            "top10ByExposure", List.of(
                                    pick(1L, "RELIANCE", "LARGE_CAP"),
                                    pick(2L, "HDFCBANK", "LARGE_CAP"),
                                    pick(3L, "INFY", "LARGE_CAP"),
                                    pick(4L, "TCS", "LARGE_CAP"),
                                    pick(5L, "ICICIBANK", "LARGE_CAP")
                            )
                    ))
                    .build();
            when(stockHoldingService.getHoldingChangeMetrics(any())).thenReturn(snapshot);

            List<HoldingChangeMetricDto.StockPerformanceChange> picks = service.pickFiveLargeCaps();

            assertThat(picks).hasSize(5);
            assertThat(picks).extracting(HoldingChangeMetricDto.StockPerformanceChange::getSymbol)
                    .containsExactly("RELIANCE", "HDFCBANK", "INFY", "TCS", "ICICIBANK");
        }
    }

    @Nested
    class OpenPosition {

        @Test
        void placesBuyAndSlInSandbox() {
            HoldingChangeMetricDto.StockPerformanceChange pick = pick(42L, "RELIANCE", "LARGE_CAP");
            StockInfoEntity stock = StockInfoEntity.builder().id(42L).symbol("RELIANCE").company("Reliance").build();

            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(stock));
            when(kiteService.placeOrder(any(KiteOrderRequestDto.class))).thenAnswer(inv -> {
                KiteOrderRequestDto req = inv.getArgument(0);
                return KiteOrderResponseDto.builder()
                        .kiteOrderId("KITE-" + req.getTransactionType() + "-" + req.getOrderType())
                        .raw("{}")
                        .sandbox(true)
                        .build();
            });
            when(kiteService.getOrderBook()).thenReturn(List.of(
                    KiteOrderDto.builder()
                            .orderId("KITE-BUY-MARKET")
                            .status("COMPLETE")
                            .price(2500.0)
                            .build()
            ));

            Optional<OrderAgentPositionEntity> opened = service.openPosition(1L, pick);

            assertThat(opened).isPresent();
            OrderAgentPositionEntity pos = opened.get();
            assertThat(pos.getSymbol()).isEqualTo("RELIANCE");
            assertThat(pos.getEntryPrice()).isEqualTo(2500.0);
            assertThat(pos.getQty()).isPositive();
            assertThat(pos.getEntryOrderId()).isEqualTo("KITE-BUY-MARKET");
            assertThat(pos.getStopLossOrderId()).isEqualTo("KITE-SELL-SL-M");
            verify(kiteService, times(2)).placeOrder(any(KiteOrderRequestDto.class));
        }

        @Test
        void marksErrorWhenFillPriceMissing() {
            HoldingChangeMetricDto.StockPerformanceChange pick = pick(42L, "RELIANCE", "LARGE_CAP");
            StockInfoEntity stock = StockInfoEntity.builder().id(42L).symbol("RELIANCE").company("Reliance").build();

            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(stock));
            when(kiteService.placeOrder(any(KiteOrderRequestDto.class))).thenReturn(
                    KiteOrderResponseDto.builder()
                            .kiteOrderId("KITE-BUY")
                            .sandbox(true)
                            .build());
            when(kiteService.getOrderBook()).thenReturn(Collections.emptyList());

            Optional<OrderAgentPositionEntity> opened = service.openPosition(1L, pick);

            assertThat(opened).isPresent();
            assertThat(opened.get().getStatus()).isEqualTo(OrderExecutionAgentService.POS_ERROR);
            // No second SL-M placeOrder because entry never filled
            verify(kiteService, times(1)).placeOrder(any(KiteOrderRequestDto.class));
        }
    }

    @Nested
    class MonitorTick {

        @Test
        void ratchetsStopLossOnRungCross() {
            OrderAgentPositionEntity pos = OrderAgentPositionEntity.builder()
                    .id(7L).cycleId(1L).stockId(42L).symbol("RELIANCE").exchange("NSE")
                    .qty(10).entryPrice(100.0).trailHigh(100.0)
                    .ladderLevel((short) 0).status(OrderExecutionAgentService.POS_OPEN)
                    .build();
            lenient().when(positionRepository.findByStatusIn(any())).thenReturn(List.of(pos));
            lenient().when(positionRepository.findByCycleId(any())).thenReturn(List.of(pos));
            lenient().when(cycleRepository.findByStatusInOrderByCreatedAtDesc(any())).thenReturn(Collections.emptyList());
            lenient().when(kiteService.getQuotes(any())).thenReturn(List.of(
                    KiteModels.KiteQuoteEnvelope.builder()
                            .quote(KiteModels.KiteQuote.builder().lastPrice(112.0).build())
                            .paidDataRequired(false).build()
            ));
            org.mockito.Mockito.lenient().doAnswer(inv -> null).when(kiteService).cancelOrder(anyString());
            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(
                    StockInfoEntity.builder().id(42L).symbol("RELIANCE").build()));
            when(kiteService.placeOrder(any(KiteOrderRequestDto.class))).thenReturn(
                    KiteOrderResponseDto.builder().kiteOrderId("KITE-NEW-SL").sandbox(true).build());

            service.tick();

            ArgumentCaptor<KiteOrderRequestDto> captor = ArgumentCaptor.forClass(KiteOrderRequestDto.class);
            verify(kiteService, Mockito.atLeast(1)).placeOrder(captor.capture());
            KiteOrderRequestDto rungSell = captor.getAllValues().stream()
                    .filter(r -> "SELL".equalsIgnoreCase(r.getTransactionType()))
                    .reduce((a, b) -> b)
                    .orElseThrow();
            // At 112, rung 1 (+5%, 105) and rung 2 (+10%, 110) are reached; rung 3 (+15%, 115) is not.
            // So ladder_level should land on 2, and SL should ratchet to trailHigh * (1 - stopLossPct) = 112 * 0.97 ≈ 108.64
            assertThat(rungSell.getTriggerPrice()).isBetween(108.0, 109.0);
            assertThat(pos.getLadderLevel()).isEqualTo((short) 2);
        }
    }

    private HoldingChangeMetricDto.StockPerformanceChange pick(Long id, String symbol, String marketCap) {
        return HoldingChangeMetricDto.StockPerformanceChange.builder()
                .id(id)
                .name(symbol + " Ltd")
                .symbol(symbol)
                .industry(IdName.builder().id(1L).name("Diversified").build())
                .sector(IdName.builder().id(1L).name("Diversified").build())
                .exposureChange(100.0)
                .relativeChangePct(1.0)
                .fundCountChange(1)
                .score(0.5)
                .marketCapCategory(marketCap)
                .build();
    }
}
