package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.SectorIndustryStockChangeDTO;
import com.rms.funds.hodings.analyser.service.SectorIndustryInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sector-insights")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SectorIndustryInsightsController {

    private final SectorIndustryInsightsService service;

    @GetMapping
    public List<SectorIndustryStockChangeDTO> getInsights(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getInsights(date);
    }
}

