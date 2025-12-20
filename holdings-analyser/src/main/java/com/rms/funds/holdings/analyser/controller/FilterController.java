package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.model.FilterAttributes;
import com.rms.funds.holdings.analyser.model.FilterCriteria;
import com.rms.funds.holdings.analyser.service.FilterAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/mf-filters")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FilterController {

    private final FilterAttributeService filterAttributeService;

    @GetMapping
    public FilterAttributes getFilterAttributes(FilterCriteria filterCriteria) {
        return filterAttributeService.getAll(filterCriteria);
    }
}
