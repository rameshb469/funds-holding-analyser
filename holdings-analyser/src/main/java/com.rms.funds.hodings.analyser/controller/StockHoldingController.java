package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.hodings.analyser.service.StockHoldingService;
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
    public HoldingChangeMetricDto getHoldingChangeMetrics(@RequestParam(name = "date", required = false)
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return stockHoldingService.getHoldingChangeMetrics(date);
    }
}
