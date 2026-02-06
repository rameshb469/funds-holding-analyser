package com.rms.funds.holdings.analyser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StockVolumeReportDto {
    private Long stockId;
    private String ticker;
    private double avgVolume;
    private double volumeStdDev;
    private int tradingDays;
    private BigDecimal avgTurnover;
    private BigDecimal lastClosePrice;
    private BigDecimal marketCap; // nullable when market cap is missing

    // explicit constructor removed; Lombok's @AllArgsConstructor will generate it
}
