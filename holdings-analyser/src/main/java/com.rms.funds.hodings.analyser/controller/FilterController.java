package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.model.FilterAttributes;
import com.rms.funds.hodings.analyser.model.FilterCriteria;
import com.rms.funds.hodings.analyser.service.FilterAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/mf-filters")
@RequiredArgsConstructor
public class FilterController {

    private final FilterAttributeService filterAttributeService;

    @GetMapping
    public FilterAttributes getFilterAttributes(FilterCriteria filterCriteria) {
        return filterAttributeService.getAll(filterCriteria);
    }
}
