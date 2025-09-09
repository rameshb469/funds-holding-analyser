package com.rms.funds.hodings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// StockInsight.java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockInsight {
    private String symbol;
    private String company;
    private String marketCapCategory; // LargeCap / MidCap / SmallCap
    private Long quantity;
    private Double netAssetPct;
    private Long marketCap;

    private Double valueChange;       // absolute change in exposure
    private Double valueChangePct;    // % change in exposure
    private Double weightChangePct;   // Δ weight in portfolio

    private String type;              // stable | growth | avoid | new
}

