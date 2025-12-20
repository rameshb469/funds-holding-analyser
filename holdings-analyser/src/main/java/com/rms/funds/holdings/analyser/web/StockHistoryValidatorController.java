package com.rms.funds.holdings.analyser.web;

import com.rms.funds.holdings.analyser.service.impl.StockHistoryValidationResult;
import com.rms.funds.holdings.analyser.service.impl.StockHistoryValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock-history")
@RequiredArgsConstructor
public class StockHistoryValidatorController {

    private final StockHistoryValidatorService validatorService;

    @GetMapping("/validate-previous")
    public ResponseEntity<List<StockHistoryValidationResult>> validatePrevious(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<StockHistoryValidationResult> results = validatorService.validatePrevious(date);
        return ResponseEntity.ok(results);
    }
}

