package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.model.ExtractorJobResponse;
import com.rms.funds.holdings.analyser.model.ExtractorJobUpdateRequest;
import com.rms.funds.holdings.analyser.service.ExtractorJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/mf-extractor-info")
@CrossOrigin("*")
public class MFDataExtractorController {

    private final ExtractorJobService extractorJobService;

    @GetMapping
    public List<ExtractorJobResponse> findAll() {
        return extractorJobService.findAll();
    }

    @PostMapping
    public List<ExtractorJobResponse> update(@RequestBody ExtractorJobUpdateRequest updateRequest) {
        return extractorJobService.upsert(updateRequest);
    }
}
