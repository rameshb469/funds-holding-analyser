package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.controller.dto.KiteLoginUrlDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderAuditDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteQuoteDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteSessionStatusDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteStockRefDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentCycleDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentPositionDto;
import com.rms.funds.holdings.analyser.controller.dto.agent.OrderAgentStatusDto;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.OrderAgentCycleRepository;
import com.rms.funds.holdings.analyser.repository.OrderAgentPositionRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.agent.OrderExecutionAgentService;
import com.rms.funds.holdings.analyser.service.kite.KiteException;
import com.rms.funds.holdings.analyser.service.kite.KiteModels;
import com.rms.funds.holdings.analyser.service.kite.KiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/broker")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class BrokerController {

    private final KiteService kiteService;
    private final StockInfoRepository stockInfoRepository;
    private final OrderAgentCycleRepository agentCycleRepository;
    private final OrderAgentPositionRepository agentPositionRepository;
    private final OrderExecutionAgentService orderAgent;

    @GetMapping("/login-url")
    public KiteLoginUrlDto loginUrl() {
        return kiteService.getLoginUrl();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("request_token") String requestToken) {
        String redirectTo = kiteService.handleCallback(requestToken);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectTo)
                .build();
    }

    @GetMapping("/session")
    public KiteSessionStatusDto session() {
        return kiteService.getSessionStatus();
    }

    @PostMapping("/orders")
    public ResponseEntity<KiteOrderResponseDto> placeOrder(@RequestBody KiteOrderRequestDto req) {
        return ResponseEntity.ok(kiteService.placeOrder(req));
    }

    @GetMapping("/orders")
    public List<KiteOrderDto> orders() {
        return kiteService.getOrderBook();
    }

    @GetMapping("/orders/audit")
    public List<KiteOrderAuditDto> audits() {
        return kiteService.getRecentAudits();
    }

    @GetMapping("/stocks")
    public List<KiteStockRefDto> stocks(@RequestParam(value = "q", required = false) String q) {
        List<StockInfoEntity> rows = (q == null || q.isBlank())
                ? stockInfoRepository.findTop500ByOrderBySymbolAsc()
                : stockInfoRepository.findTop20BySymbolContainingIgnoreCaseOrderBySymbolAsc(q.trim());
        return rows.stream()
                .map(s -> KiteStockRefDto.builder()
                        .id(s.getId())
                        .symbol(s.getSymbol())
                        .company(s.getCompany())
                        .exchange("NSE")
                        .isinNumber(s.getIsinNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/quote")
    public KiteQuoteDto quote(@RequestParam("exchange") String exchange,
                              @RequestParam("symbol") String symbol) {
        KiteModels.KiteQuoteEnvelope env = kiteService.getQuote(exchange, symbol);
        return toQuoteDto(env);
    }

    @GetMapping("/agent/status")
    public OrderAgentStatusDto agentStatus() {
        return orderAgent.status();
    }

    @GetMapping("/agent/cycles")
    public List<OrderAgentCycleDto> agentCycles() {
        return agentCycleRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(orderAgent::toCycleDto)
                .toList();
    }

    @GetMapping("/agent/cycles/{id}/positions")
    public List<OrderAgentPositionDto> agentPositions(@PathVariable("id") Long cycleId) {
        return agentPositionRepository.findByCycleId(cycleId).stream()
                .map(orderAgent::toPositionDto)
                .toList();
    }

    @PostMapping("/agent/trigger")
    public OrderAgentCycleDto agentTrigger() {
        return orderAgent.triggerNow();
    }

    @PostMapping("/agent/stop")
    public Map<String, Object> agentStop() {
        List<String> tail = orderAgent.stopMonitor();
        return Map.of("stopped", true, "tail", tail);
    }

    private KiteQuoteDto toQuoteDto(KiteModels.KiteQuoteEnvelope env) {
        if (env == null) {
            return KiteQuoteDto.builder().paidDataRequired(false).build();
        }
        KiteModels.KiteQuote q = env.getQuote();
        KiteQuoteDto.KiteOhlcDto ohlc = q != null && q.getOhlc() != null
                ? KiteQuoteDto.KiteOhlcDto.builder()
                        .open(q.getOhlc().getOpen())
                        .high(q.getOhlc().getHigh())
                        .low(q.getOhlc().getLow())
                        .close(q.getOhlc().getClose())
                        .build()
                : null;
        KiteQuoteDto.KiteDepthDto depth = q != null && q.getDepth() != null
                ? KiteQuoteDto.KiteDepthDto.builder()
                        .buy(q.getDepth().getBuy() == null ? java.util.List.of() : q.getDepth().getBuy().stream()
                                .map(l -> KiteQuoteDto.KiteDepthLevelDto.builder()
                                        .price(l.getPrice())
                                        .quantity(l.getQuantity())
                                        .orders(l.getOrders())
                                        .build())
                                .collect(Collectors.toList()))
                        .sell(q.getDepth().getSell() == null ? java.util.List.of() : q.getDepth().getSell().stream()
                                .map(l -> KiteQuoteDto.KiteDepthLevelDto.builder()
                                        .price(l.getPrice())
                                        .quantity(l.getQuantity())
                                        .orders(l.getOrders())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()
                : null;
        return KiteQuoteDto.builder()
                .instrumentToken(q == null ? null : q.getInstrumentToken())
                .tradingSymbol(q == null ? null : q.getTradingSymbol())
                .exchange(q == null ? null : q.getExchange())
                .lastPrice(q == null ? null : q.getLastPrice())
                .change(q == null ? null : q.getChange())
                .ohlc(ohlc)
                .depth(depth)
                .volume(q == null ? null : q.getVolume())
                .averagePrice(q == null ? null : q.getAveragePrice())
                .paidDataRequired(env.isPaidDataRequired()
                        || (q != null && q.getDepth() == null && (q.getLastPrice() == null || q.getLastPrice() == 0.0)))
                .errorType(env.getErrorType())
                .message(env.getMessage())
                .build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(KiteException.class)
    public ResponseEntity<Map<String, Object>> handleKiteException(KiteException ex) {
        log.warn("Kite error {} {}: {}", ex.getStatusCode(), ex.getErrorType(), ex.getMessage());
        HttpStatus status = ex.isSessionExpired() ? HttpStatus.CONFLICT
                : HttpStatus.valueOf(ex.getStatusCode() == 0 ? 502 : ex.getStatusCode());
        return ResponseEntity.status(status).body(Map.of(
                "status", "error",
                "kiteStatus", ex.getKiteStatus() == null ? "" : ex.getKiteStatus(),
                "errorType", ex.getErrorType() == null ? "" : ex.getErrorType(),
                "message", ex.getMessage()
        ));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", ex.getMessage() == null ? "Bad request" : ex.getMessage()
        ));
    }
}
