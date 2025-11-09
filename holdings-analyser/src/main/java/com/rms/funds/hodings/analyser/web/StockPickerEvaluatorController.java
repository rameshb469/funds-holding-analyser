package com.rms.funds.hodings.analyser.web;

import com.rms.funds.hodings.analyser.service.impl.StockPickerEvaluatorService;
import com.rms.funds.hodings.analyser.service.impl.StockPickerEvaluatorService.EvaluationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stock-picker")
@RequiredArgsConstructor
public class StockPickerEvaluatorController {

    private final StockPickerEvaluatorService evaluatorService;

    @GetMapping("/evaluate")
    public ResponseEntity<EvaluationSummary> evaluate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate,
            @RequestParam(required = false, defaultValue = "1") int lookbackDays,
            @RequestParam(required = false, defaultValue = "10") int topN,
            @RequestParam(required = false, defaultValue = "1") int holdDays
    ) {
        EvaluationSummary summary = evaluatorService.evaluate(tradeDate, lookbackDays, topN, holdDays);
        return ResponseEntity.ok(summary);
    }
}

