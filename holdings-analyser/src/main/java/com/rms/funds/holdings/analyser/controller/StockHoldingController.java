package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.holdings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.holdings.analyser.model.HoldingChangeMetricFilter;
import com.rms.funds.holdings.analyser.model.MarketCapCategoryType;
import com.rms.funds.holdings.analyser.service.StockHoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/stock-holdings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StockHoldingController {

    private final StockHoldingService stockHoldingService;

    @GetMapping(value = "/{stockId}/metrics")
    public StockHoldingDto getMetrics(@PathVariable("stockId") Long stockId) {
        return stockHoldingService.getMetrics(stockId);
    }

    @GetMapping(value = "/metrics")
    public HoldingChangeMetricDto getHoldingChangeMetrics(@RequestParam(value = "marketCapCategory", required = false) String marketCapCategory,
                                                          @RequestParam(value = "sector", required = false) String sector,
                                                          @RequestParam(value = "industry", required = false) String industry,
                                                          @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return stockHoldingService.getHoldingChangeMetrics(HoldingChangeMetricFilter.builder()
                        .date(date)
                        .industryId(industry)
                        .sectorId(sector)
                        .marketCapCategory(MarketCapCategoryType.fromId(marketCapCategory))
                .build());
    }
}


