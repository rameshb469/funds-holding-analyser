package com.rms.funds.holdings.analyser.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StockInsight {
    private Long stockId;
    private String symbol;
    private String company;
    private String marketCapCategory;
    private Long quantityCurr;
    private Long quantityPrev;
    private Double valueCurr;
    private Double valuePrev;
    private Double netAssetPctCurr;
    private Double netAssetPctPrev;
    private Double weightChangePct;
    private Double quantityChangePct;
    private Double valueChangePct;
    private List<String> signals;
    private int score;
    private String recommendation;
}
