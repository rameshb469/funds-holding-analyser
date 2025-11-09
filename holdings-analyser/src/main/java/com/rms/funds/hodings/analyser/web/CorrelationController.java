package com.rms.funds.hodings.analyser.web;

import com.rms.funds.hodings.analyser.service.impl.CorrelationAnalysisService;
import com.rms.funds.hodings.analyser.service.impl.CorrelationResult;
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
@RequestMapping("/api/correlations")
@RequiredArgsConstructor
public class CorrelationController {

    private final CorrelationAnalysisService correlationService;

    @GetMapping
    public ResponseEntity<List<CorrelationResult>> getCorrelations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false, defaultValue = "50") int topN
    ) {
        LocalDate endDate = (end != null) ? end : LocalDate.now().minusDays(1);
        LocalDate startDate = (start != null) ? start : endDate.minusMonths(6);

        List<CorrelationResult> results = correlationService.computeTopCorrelations(startDate, endDate, topN);
        return ResponseEntity.ok(results);
    }
}

