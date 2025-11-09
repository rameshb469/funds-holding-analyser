package com.rms.funds.hodings.analyser.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationResult {
    private Long stockId1;
    private Long stockId2;
    private String symbol1;
    private String symbol2;
    private double correlation;
    private int overlap;
}


