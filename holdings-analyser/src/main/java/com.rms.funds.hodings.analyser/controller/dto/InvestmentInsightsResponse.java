package com.rms.funds.hodings.analyser.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class InvestmentInsightsResponse {
    private LocalDate currDate;
    private LocalDate prevDate;
    private List<StockInsight> stocks; // All stocks with metrics & signals
}
