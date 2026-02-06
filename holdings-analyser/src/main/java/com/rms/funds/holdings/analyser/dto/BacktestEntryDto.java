package com.rms.funds.holdings.analyser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BacktestEntryDto {
    private LocalDate windowStart;
    private LocalDate windowEnd;
    private double avgForwardReturn; // decimal (e.g. 0.05 = 5%)
    private int picksSelected;
    private int picksWithForwardData;
}

