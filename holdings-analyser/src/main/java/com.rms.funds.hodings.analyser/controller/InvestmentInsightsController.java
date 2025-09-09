package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.InvestmentInsightsResponse;
import com.rms.funds.hodings.analyser.service.InvestmentInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/investment-insights")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvestmentInsightsController {

    private final InvestmentInsightsService insightsService;

    @GetMapping
    public ResponseEntity<InvestmentInsightsResponse> getInsights(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        InvestmentInsightsResponse response = insightsService.getInsights(date);
        return ResponseEntity.ok(response);
    }
}

