package com.rms.funds.hodings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// InvestmentInsightsResponse.java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestmentInsightsResponse {

    private LocalDate currDate;
    private LocalDate prevDate;

    private Map<String, List<StockInsight>> stable;
    private Map<String, List<StockInsight>> growth;
    private Map<String, List<StockInsight>> avoid;
    private Map<String, List<StockInsight>> newlyAdded;
}

