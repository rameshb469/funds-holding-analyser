package com.rms.funds.holdings.analyser.service.kite;

import com.rms.funds.holdings.analyser.config.KiteConfigProperties;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderAuditDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.entity.KiteOrderAuditEntity;
import com.rms.funds.holdings.analyser.entity.KiteSessionEntity;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.KiteOrderAuditRepository;
import com.rms.funds.holdings.analyser.repository.KiteSessionRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KiteServiceImplTest {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String SESSION_API_KEY = "test-api-key";

    @Mock
    private KiteClient kiteClient;

    @Mock
    private KiteSessionRepository sessionRepository;

    @Mock
    private KiteOrderAuditRepository auditRepository;

    @Mock
    private StockInfoRepository stockInfoRepository;

    @Spy
    private KiteConfigProperties props = new KiteConfigProperties();

    @InjectMocks
    private KiteServiceImpl service;

    private KiteSessionEntity activeSession() {
        return KiteSessionEntity.builder()
                .id(1L)
                .accessToken(ACCESS_TOKEN)
                .userId("AB1234")
                .userName("Test User")
                .apiKey(SESSION_API_KEY)
                .loginAt(LocalDateTime.now().minusMinutes(1))
                .expiresAt(LocalDateTime.now().plusHours(6))
                .build();
    }

    private KiteOrderRequestDto validOrder() {
        return KiteOrderRequestDto.builder()
                .symbol("INFY")
                .exchange("NSE")
                .transactionType("BUY")
                .orderType("MARKET")
                .product("CNC")
                .quantity(10)
                .validity("DAY")
                .build();
    }

    @BeforeEach
    void setUp() {
        lenient().when(auditRepository.save(any(KiteOrderAuditEntity.class)))
                .thenAnswer(inv -> {
                    KiteOrderAuditEntity entity = inv.getArgument(0);
                    entity.setId(42L);
                    return entity;
                });
    }

    @Nested
    class LoginUrl {

        @Test
        void buildsUrlWhenApiKeyConfigured() {
            props.setApiKey("abc123");
            when(kiteClient.buildLoginUrl()).thenReturn("https://kite.zerodha.com/connect/login?api_key=abc123&v=3");

            var dto = service.getLoginUrl();

            assertThat(dto.getUrl()).contains("api_key=abc123");
        }

        @Test
        void rejectsWhenApiKeyMissing() {
            props.setApiKey("");
            assertThatThrownBy(() -> service.getLoginUrl())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("KITE_API_KEY");
        }
    }

    @Nested
    class Callback {

        @Test
        void persistsSessionRowOnTokenExchange() {
            props.setApiKey("k");
            props.setApiSecret("s");
            var token = KiteModels.KiteTokenResponse.builder()
                    .accessToken(ACCESS_TOKEN)
                    .userId("AB1234")
                    .userName("Test User")
                    .apiKey("k")
                    .build();
            when(kiteClient.exchangeRequestToken("req-token")).thenReturn(token);

            String redirect = service.handleCallback("req-token");

            assertThat(redirect).isEqualTo("/broker/orders?status=connected");
            ArgumentCaptor<KiteSessionEntity> captor = ArgumentCaptor.forClass(KiteSessionEntity.class);
            verify(sessionRepository).save(captor.capture());
            KiteSessionEntity saved = captor.getValue();
            assertThat(saved.getId()).isEqualTo(1L);
            assertThat(saved.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(saved.getUserId()).isEqualTo("AB1234");
            assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(5));
        }
    }

    @Nested
    class SessionStatus {

        @Test
        void reportsDisconnectedWhenNoRow() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

            var status = service.getSessionStatus();

            assertThat(status.isConnected()).isFalse();
            assertThat(status.getUserId()).isNull();
        }

        @Test
        void reportsConnectedWhenSessionLive() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));

            var status = service.getSessionStatus();

            assertThat(status.isConnected()).isTrue();
            assertThat(status.getUserId()).isEqualTo("AB1234");
            assertThat(status.getUserName()).isEqualTo("Test User");
        }

        @Test
        void reportsDisconnectedWhenTokenExpired() {
            KiteSessionEntity expired = activeSession().toBuilder()
                    .expiresAt(LocalDateTime.now().minusHours(1))
                    .build();
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(expired));

            var status = service.getSessionStatus();

            assertThat(status.isConnected()).isFalse();
        }
    }

    @Nested
    class PlaceOrderValidation {

        @Test
        void rejectsWhenBothSymbolAndStockIdBlank() {
            KiteOrderRequestDto req = validOrder();
            req.setSymbol("");

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("symbol or stockId");
        }

        @Test
        void rejectsUnknownTransactionType() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("HOLD");

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("transactionType");
        }

        @Test
        void rejectsNonPositiveQuantity() {
            KiteOrderRequestDto req = validOrder();
            req.setQuantity(0);

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        void rejectsUnknownOrderType() {
            KiteOrderRequestDto req = validOrder();
            req.setOrderType("GTT");

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("orderType");
        }

        @Test
        void rejectsUnknownProduct() {
            KiteOrderRequestDto req = validOrder();
            req.setProduct("NRML-M");

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("product");
        }

        @Test
        void validationFailsBeforeKiteCallEvenInSandbox() {
            props.setSandbox(true);
            KiteOrderRequestDto req = validOrder();
            req.setSymbol("");

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(kiteClient, never()).placeOrder(any(), anyString());
            verify(auditRepository, never()).save(any());
        }
    }

    @Nested
    class PlaceOrderSandbox {

        @BeforeEach
        void enableSandbox() {
            props.setSandbox(true);
        }

        @Test
        void returnsSandboxIdAndAudits() {
            KiteOrderRequestDto req = validOrder();

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(resp.isSandbox()).isTrue();
            assertThat(resp.getKiteOrderId()).startsWith("SANDBOX-");
            assertThat(resp.getAuditId()).isEqualTo(42L);
            verify(auditRepository, times(1)).save(any(KiteOrderAuditEntity.class));
            verify(kiteClient, never()).placeOrder(any(), anyString());
        }

        @Test
        void handlesSellInSandboxWithoutPositionLookup() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(resp.isSandbox()).isTrue();
            verify(kiteClient, never()).getPositions(anyString());
        }
    }

    @Nested
    class PlaceOrderLive {

        @BeforeEach
        void enableLive() {
            props.setSandbox(false);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
        }

        @Test
        void placesBuyAndAuditsResponse() {
            KiteOrderRequestDto req = validOrder();
            when(kiteClient.placeOrder(eq(req), eq(ACCESS_TOKEN)))
                    .thenReturn(KiteModels.KiteOrderResponse.builder()
                            .orderId("KITE-001")
                            .raw("{\"order_id\":\"KITE-001\",\"status\":\"OPEN\"}")
                            .build());

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(resp.getKiteOrderId()).isEqualTo("KITE-001");
            assertThat(resp.isSandbox()).isFalse();
            assertThat(resp.getStatus()).contains("KITE-001");
            assertThat(resp.getAuditId()).isEqualTo(42L);
            verify(kiteClient, never()).getPositions(anyString());
        }

        @Test
        void rejectsWhenNoSession() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.placeOrder(validOrder()))
                    .isInstanceOf(KiteException.class)
                    .extracting("statusCode").isEqualTo(401);
            verify(kiteClient, never()).placeOrder(any(), anyString());
        }

        @Test
        void sellGuardAcceptsWhenHeldQuantitySufficient() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            req.setQuantity(5);
            when(kiteClient.getPositions(ACCESS_TOKEN)).thenReturn(List.of(
                    KiteModels.KitePosition.builder()
                            .exchange("NSE").tradingSymbol("INFY").quantity(10).build()
            ));
            when(kiteClient.placeOrder(eq(req), eq(ACCESS_TOKEN)))
                    .thenReturn(KiteModels.KiteOrderResponse.builder()
                            .orderId("KITE-S1").raw("{\"order_id\":\"KITE-S1\"}").build());

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(resp.getKiteOrderId()).isEqualTo("KITE-S1");
        }

        @Test
        void sellGuardRejectsWhenNoPosition() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            when(kiteClient.getPositions(ACCESS_TOKEN)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no open position");
            verify(kiteClient, never()).placeOrder(any(), anyString());
        }

        @Test
        void sellGuardRejectsWhenQuantityExceedsHeld() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            req.setQuantity(50);
            when(kiteClient.getPositions(ACCESS_TOKEN)).thenReturn(List.of(
                    KiteModels.KitePosition.builder()
                            .exchange("NSE").tradingSymbol("INFY").quantity(10).build()
            ));

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("only 10 available");
            verify(kiteClient, never()).placeOrder(any(), anyString());
        }

        @Test
        void sellGuardIgnoresPositionOnOtherExchange() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            when(kiteClient.getPositions(ACCESS_TOKEN)).thenReturn(List.of(
                    KiteModels.KitePosition.builder()
                            .exchange("BSE").tradingSymbol("INFY").quantity(10).build()
            ));

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no open position");
        }

        @Test
        void sellGuardRethrowsSessionExpired() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            when(kiteClient.getPositions(ACCESS_TOKEN))
                    .thenThrow(new KiteException(401, "error", "TokenException", "expired"));

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(KiteException.class)
                    .extracting("statusCode").isEqualTo(401);
            verify(kiteClient, never()).placeOrder(any(), anyString());
        }

        @Test
        void sellGuardSkipsOnTransientError() {
            KiteOrderRequestDto req = validOrder();
            req.setTransactionType("SELL");
            req.setQuantity(5);
            when(kiteClient.getPositions(ACCESS_TOKEN))
                    .thenThrow(new KiteException(500, "error", "Server", "boom"));
            when(kiteClient.placeOrder(eq(req), eq(ACCESS_TOKEN)))
                    .thenReturn(KiteModels.KiteOrderResponse.builder()
                            .orderId("KITE-S2").raw("{\"order_id\":\"KITE-S2\"}").build());

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(resp.getKiteOrderId()).isEqualTo("KITE-S2");
        }
    }

    @Nested
    class PlaceOrderStockIdResolution {

        @BeforeEach
        void enableSandbox() {
            props.setSandbox(true);
        }

        @Test
        void rejectsUnknownStockId() {
            KiteOrderRequestDto req = validOrder();
            req.setStockId(999L);
            req.setSymbol(null);
            when(stockInfoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.placeOrder(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown stockId");
            verify(auditRepository, never()).save(any());
        }

        @Test
        void resolvesSymbolFromStockDetailsEntity() {
            KiteOrderRequestDto req = validOrder();
            req.setStockId(42L);
            req.setSymbol(null);
            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(
                    StockInfoEntity.builder().id(42L).symbol("INFY").build()
            ));

            KiteOrderResponseDto resp = service.placeOrder(req);

            assertThat(req.getSymbol()).isEqualTo("INFY");
            assertThat(resp.getKiteOrderId()).startsWith("SANDBOX-");
            ArgumentCaptor<KiteOrderAuditEntity> captor = ArgumentCaptor.forClass(KiteOrderAuditEntity.class);
            verify(auditRepository).save(captor.capture());
            KiteOrderAuditEntity saved = captor.getValue();
            assertThat(saved.getStockId()).isEqualTo(42L);
            assertThat(saved.getSymbol()).isEqualTo("INFY");
        }

        @Test
        void prefersEntitySymbolOverRequestSymbol() {
            KiteOrderRequestDto req = validOrder();
            req.setStockId(42L);
            req.setSymbol("STALE");
            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(
                    StockInfoEntity.builder().id(42L).symbol("INFY").build()
            ));

            service.placeOrder(req);

            assertThat(req.getSymbol()).isEqualTo("INFY");
        }

        @Test
        void fillsExchangeFromDefaultWhenBlank() {
            KiteOrderRequestDto req = validOrder();
            req.setStockId(42L);
            req.setSymbol(null);
            req.setExchange(null);
            when(stockInfoRepository.findById(42L)).thenReturn(Optional.of(
                    StockInfoEntity.builder().id(42L).symbol("INFY").build()
            ));

            service.placeOrder(req);

            assertThat(req.getExchange()).isEqualTo("NSE");
        }

        @Test
        void noLookupWhenStockIdAbsent() {
            KiteOrderRequestDto req = validOrder();

            service.placeOrder(req);

            verify(stockInfoRepository, never()).findById(any());
            assertThat(req.getSymbol()).isEqualTo("INFY");
        }
    }

    @Nested
    class OrderBookAndAudit {

        @Test
        void orderBookEmptyInSandbox() {
            props.setSandbox(true);
            assertThat(service.getOrderBook()).isEmpty();
        }

        @Test
        void orderBookEmptyWithoutSession() {
            props.setSandbox(false);
            when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThat(service.getOrderBook()).isEmpty();
        }

        @Test
        void orderBookMappedFromKite() {
            props.setSandbox(false);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
            when(kiteClient.getOrderBook(ACCESS_TOKEN)).thenReturn(List.of(
                    KiteModels.KiteOrderEntry.builder()
                            .orderId("KITE-A")
                            .exchange("NSE")
                            .tradingSymbol("TCS")
                            .transactionType("BUY")
                            .orderType("MARKET")
                            .product("CNC")
                            .quantity(3)
                            .price(3500.0)
                            .status("COMPLETE")
                            .orderTimestamp("2026-06-14 09:30:00")
                            .build()
            ));

            var orders = service.getOrderBook();

            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getOrderId()).isEqualTo("KITE-A");
            assertThat(orders.get(0).getTradingSymbol()).isEqualTo("TCS");
        }

        @Test
        void orderBookSwallowsSessionExpired() {
            props.setSandbox(false);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession()));
            when(kiteClient.getOrderBook(ACCESS_TOKEN))
                    .thenThrow(new KiteException(401, "error", "TokenException", "expired"));

            assertThat(service.getOrderBook()).isEmpty();
        }

        @Test
        void auditsListMappedFromRepository() {
            KiteOrderAuditEntity entity = KiteOrderAuditEntity.builder()
                    .id(7L)
                    .symbol("INFY")
                    .exchange("NSE")
                    .transactionType("SELL")
                    .orderType("LIMIT")
                    .product("CNC")
                    .quantity(2)
                    .price(1500.0)
                    .kiteOrderId("KITE-X")
                    .status("OPEN")
                    .sandbox(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(auditRepository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

            List<KiteOrderAuditDto> audits = service.getRecentAudits();

            assertThat(audits).hasSize(1);
            KiteOrderAuditDto dto = audits.get(0);
            assertThat(dto.getId()).isEqualTo(7L);
            assertThat(dto.getSymbol()).isEqualTo("INFY");
            assertThat(dto.getTransactionType()).isEqualTo("SELL");
            assertThat(dto.getKiteOrderId()).isEqualTo("KITE-X");
            assertThat(dto.isSandbox()).isFalse();
        }
    }
}
