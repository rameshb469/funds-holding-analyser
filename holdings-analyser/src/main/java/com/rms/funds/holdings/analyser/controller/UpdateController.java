package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.service.MarketCapUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/api/v1/update")
@RequiredArgsConstructor
@Slf4j
public class UpdateController {

    private final MarketCapUpdateService marketCapUpdateService;

    @PostMapping(value = "/market-caps")
    public ResponseEntity<Void> updateMarketCaps() {
        try {
            marketCapUpdateService.updateMarketCap();
        } catch (Exception e) {
            log.error("Error updating market caps", e);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }
}
