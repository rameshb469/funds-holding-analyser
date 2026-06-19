package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.controller.dto.KiteLoginUrlDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderAuditDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderRequestDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteOrderResponseDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteSessionStatusDto;
import com.rms.funds.holdings.analyser.controller.dto.KiteStockRefDto;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.kite.KiteException;
import com.rms.funds.holdings.analyser.service.kite.KiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
