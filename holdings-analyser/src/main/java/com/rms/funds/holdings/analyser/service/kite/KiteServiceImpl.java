package com.rms.funds.holdings.analyser.service.kite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.config.KiteConfigProperties;
import com.rms.funds.holdings.analyser.controller.dto.KiteLoginUrlDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderAuditDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteSessionStatusDto;
import com.rms.funds.holdings.analyser.entity.KiteOrderAuditEntity;
import com.rms.funds.holdings.analyser.entity.KiteSessionEntity;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.KiteOrderAuditRepository;
import com.rms.funds.holdings.analyser.repository.KiteSessionRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiteServiceImpl implements KiteService {

    private static final long SESSION_ROW_ID = 1L;
    private static final long KITE_TOKEN_TTL_HOURS = 6;

    private final KiteClient kiteClient;
    private final KiteSessionRepository sessionRepository;
    private final KiteOrderAuditRepository auditRepository;
    private final StockInfoRepository stockInfoRepository;
    private final KiteConfigProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public KiteLoginUrlDto getLoginUrl() {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalStateException("KITE_API_KEY is not configured");
        }
        return KiteLoginUrlDto.builder().url(kiteClient.buildLoginUrl()).build();
    }

    @Override
    @Transactional
    public String handleCallback(String requestToken) {
        KiteModels.KiteTokenResponse token = kiteClient.exchangeRequestToken(requestToken);
        KiteSessionEntity session = KiteSessionEntity.builder()
                .id(SESSION_ROW_ID)
                .accessToken(token.getAccessToken())
                .userId(token.getUserId())
                .userName(token.getUserName())
                .apiKey(token.getApiKey() != null ? token.getApiKey() : props.getApiKey())
                .loginAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(KITE_TOKEN_TTL_HOURS))
                .build();
        sessionRepository.save(session);
        return "/broker/orders?status=connected";
    }

    @Override
    public KiteSessionStatusDto getSessionStatus() {
        return sessionRepository.findById(SESSION_ROW_ID)
                .map(this::toStatus)
                .orElseGet(() -> KiteSessionStatusDto.builder()
                        .connected(false)
                        .sandbox(props.isSandbox())
                        .build());
    }

    @Override
    @Transactional
    public KiteOrderResponseDto placeOrder(KiteOrderRequestDto req) {
        validate(req);
        resolveSymbolFromStockDetails(req);

        String requestJson = toJson(req);

        if (props.isSandbox()) {
            KiteOrderResponseDto resp = sandboxResponse(req);
            saveAudit(req, resp, requestJson, true);
            return resp;
        }

        KiteSessionEntity session = requireSession();
        if (req.getTransactionType().equalsIgnoreCase("SELL")) {
            enforceSellPreFlight(req, session.getAccessToken());
        }
        KiteModels.KiteOrderResponse kiteResp = kiteClient.placeOrder(req, session.getAccessToken());
        KiteOrderResponseDto resp = KiteOrderResponseDto.builder()
                .kiteOrderId(kiteResp.getOrderId())
                .status(kiteResp.getRaw())
                .raw(kiteResp.getRaw())
                .sandbox(false)
                .build();
        saveAudit(req, resp, requestJson, false);
        return resp;
    }

    @Override
    public List<KiteOrderDto> getOrderBook() {
        if (props.isSandbox()) {
            return Collections.emptyList();
        }
        Optional<KiteSessionEntity> session = sessionRepository.findById(SESSION_ROW_ID);
        if (session.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return kiteClient.getOrderBook(session.get().getAccessToken()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (KiteException ex) {
            log.warn("Order book fetch failed: {} {}", ex.getStatusCode(), ex.getMessage());
            if (ex.isSessionExpired()) {
                return Collections.emptyList();
            }
            throw ex;
        }
    }

    @Override
    public List<KiteOrderAuditDto> getRecentAudits() {
        return auditRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toAuditDto)
                .collect(Collectors.toList());
    }

    private void validate(KiteOrderRequestDto req) {
        if (req.getStockId() == null
                && (req.getTradingsymbol() == null || req.getTradingsymbol().isBlank())) {
            throw new IllegalArgumentException("symbol or stockId is required");
        }
        if (req.getTransactionType() == null
                || !(req.getTransactionType().equalsIgnoreCase("BUY") || req.getTransactionType().equalsIgnoreCase("SELL"))) {
            throw new IllegalArgumentException("transactionType must be BUY or SELL");
        }
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (req.getOrderType() == null
                || !(req.getOrderType().equalsIgnoreCase("MARKET") || req.getOrderType().equalsIgnoreCase("LIMIT")
                || req.getOrderType().equalsIgnoreCase("SL") || req.getOrderType().equalsIgnoreCase("SL-M"))) {
            throw new IllegalArgumentException("orderType must be MARKET, LIMIT, SL, or SL-M");
        }
        if (req.getProduct() == null
                || !(req.getProduct().equalsIgnoreCase("CNC") || req.getProduct().equalsIgnoreCase("MIS")
                || req.getProduct().equalsIgnoreCase("NRML"))) {
            throw new IllegalArgumentException("product must be CNC, MIS, or NRML");
        }
    }

    private KiteSessionEntity requireSession() {
        return sessionRepository.findById(SESSION_ROW_ID)
                .filter(s -> s.getAccessToken() != null && !s.getAccessToken().isBlank())
                .orElseThrow(() -> new KiteException(401, "error", "SessionExpired",
                        "No active Kite session. Please log in to Zerodha first."));
    }

    private void enforceSellPreFlight(KiteOrderRequestDto req, String accessToken) {
        List<KiteModels.KitePosition> positions;
        try {
            positions = kiteClient.getPositions(accessToken);
        } catch (KiteException ex) {
            if (ex.isSessionExpired()) {
                throw ex;
            }
            log.warn("Skipping SELL pre-flight; could not fetch positions: {} {}", ex.getStatusCode(), ex.getMessage());
            return;
        }
        int held = positions.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .filter(p -> matchesPosition(req, p))
                .mapToInt(KiteModels.KitePosition::getQuantity)
                .sum();
        if (held <= 0) {
            throw new IllegalArgumentException(
                    "SELL rejected: no open position for " + req.getTradingsymbol() + " on " + req.getExchange()
                            + " (CN/MIS positions fetched from Kite show zero or negative quantity).");
        }
        if (req.getQuantity() != null && req.getQuantity() > held) {
            throw new IllegalArgumentException(
                    "SELL rejected: requested " + req.getQuantity() + " shares of " + req.getTradingsymbol()
                            + " but Kite positions show only " + held + " available.");
        }
    }

    private boolean matchesPosition(KiteOrderRequestDto req, KiteModels.KitePosition p) {
        if (p.getTradingSymbol() == null || !p.getTradingSymbol().equalsIgnoreCase(req.getTradingsymbol())) {
            return false;
        }
        if (req.getExchange() != null && !req.getExchange().isBlank()
                && p.getExchange() != null && !p.getExchange().equalsIgnoreCase(req.getExchange())) {
            return false;
        }
        return true;
    }

    private KiteOrderResponseDto sandboxResponse(KiteOrderRequestDto req) {
        String fakeId = "SANDBOX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String body = String.format("{\"order_id\":\"%s\",\"status\":\"PLACED (sandbox)\"}", fakeId);
        return KiteOrderResponseDto.builder()
                .kiteOrderId(fakeId)
                .status(body)
                .raw(body)
                .sandbox(true)
                .build();
    }

    private void saveAudit(KiteOrderRequestDto req, KiteOrderResponseDto resp, String requestJson, boolean sandbox) {
        KiteOrderAuditEntity audit = KiteOrderAuditEntity.builder()
                .stockId(req.getStockId())
                .symbol(req.getTradingsymbol())
                .exchange(req.getExchange())
                .transactionType(req.getTransactionType())
                .orderType(req.getOrderType())
                .product(req.getProduct())
                .quantity(req.getQuantity())
                .price(req.getPrice())
                .triggerPrice(req.getTriggerPrice())
                .tag(req.getTag())
                .validity(req.getValidity())
                .kiteOrderId(resp.getKiteOrderId())
                .status(truncate(resp.getStatus(), 2000))
                .requestJson(truncate(requestJson, 4000))
                .responseJson(truncate(resp.getRaw(), 4000))
                .sandbox(sandbox)
                .createdAt(LocalDateTime.now())
                .build();
        KiteOrderAuditEntity saved = auditRepository.save(audit);
        resp.setAuditId(saved.getId());
    }

    private KiteSessionStatusDto toStatus(KiteSessionEntity s) {
        boolean live = s.getAccessToken() != null && !s.getAccessToken().isBlank()
                && (s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()));
        return KiteSessionStatusDto.builder()
                .connected(live)
                .userId(s.getUserId())
                .userName(s.getUserName())
                .loginAt(s.getLoginAt())
                .expiresAt(s.getExpiresAt())
                .sandbox(props.isSandbox())
                .build();
    }

    private KiteOrderDto toDto(KiteModels.KiteOrderEntry e) {
        return KiteOrderDto.builder()
                .orderId(e.getOrderId())
                .exchange(e.getExchange())
                .tradingSymbol(e.getTradingSymbol())
                .transactionType(e.getTransactionType())
                .orderType(e.getOrderType())
                .product(e.getProduct())
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .triggerPrice(e.getTriggerPrice())
                .status(e.getStatus())
                .orderTimestamp(e.getOrderTimestamp())
                .tag(e.getTag())
                .build();
    }

    private KiteOrderAuditDto toAuditDto(KiteOrderAuditEntity e) {
        return KiteOrderAuditDto.builder()
                .id(e.getId())
                .stockId(e.getStockId())
                .symbol(e.getSymbol())
                .exchange(e.getExchange())
                .transactionType(e.getTransactionType())
                .orderType(e.getOrderType())
                .product(e.getProduct())
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .triggerPrice(e.getTriggerPrice())
                .tag(e.getTag())
                .validity(e.getValidity())
                .kiteOrderId(e.getKiteOrderId())
                .status(e.getStatus())
                .sandbox(e.isSandbox())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return String.valueOf(o);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * If the caller supplied a stockId, look the row up in stock_details and
     * stamp the request with the entity's authoritative `symbol` column value
     * (and the stockId, if missing). This is what the Broker page uses to
     * guarantee the symbol we send to Kite is the exact same string the rest
     * of the app uses for that stock.
     */
    private void resolveSymbolFromStockDetails(KiteOrderRequestDto req) {
        if (req.getStockId() == null) {
            return;
        }
        Optional<StockInfoEntity> stock = stockInfoRepository.findById(req.getStockId());
        if (stock.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown stockId " + req.getStockId() + " — pick a row from the stock list.");
        }
        StockInfoEntity s = stock.get();
        if (s.getSymbol() == null || s.getSymbol().isBlank()) {
            throw new IllegalArgumentException(
                    "stock_details row " + s.getId() + " has no symbol value.");
        }
        req.setTradingsymbol(s.getSymbol());
        if (req.getExchange() == null || req.getExchange().isBlank()) {
            req.setExchange("NSE");
        }
    }
}
