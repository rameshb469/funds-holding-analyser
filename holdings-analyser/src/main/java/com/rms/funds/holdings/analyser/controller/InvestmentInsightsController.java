package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.controller.dto.InvestmentInsightsResponse;
import com.rms.funds.holdings.analyser.model.HoldingChangeMetricFilter;
import com.rms.funds.holdings.analyser.model.MarketCapCategoryType;
import com.rms.funds.holdings.analyser.service.InvestmentInsightsService;
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
            @RequestParam(value = "marketCapCategory", required = false) String marketCapCategory,
            @RequestParam(value = "sector", required = false) String sector,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
            ) {


        InvestmentInsightsResponse response = insightsService.getInsights(HoldingChangeMetricFilter.builder()
                .date(date)
                .industryId(industry)
                .sectorId(sector)
                .marketCapCategory(MarketCapCategoryType.fromId(marketCapCategory))
                .build());
        return ResponseEntity.ok(response);
    }
}

