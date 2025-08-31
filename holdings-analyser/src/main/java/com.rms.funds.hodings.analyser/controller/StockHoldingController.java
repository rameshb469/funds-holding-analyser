package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.hodings.analyser.service.StockHoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
