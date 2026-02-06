package com.rms.funds.holdings.analyser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
public class HoldingChangeMetricFilter {

    private MarketCapCategoryType marketCapCategory;
    private String sectorId;
    private String industryId;
    private LocalDate date;
}
