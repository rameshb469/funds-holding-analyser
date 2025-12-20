package com.rms.funds.holdings.analyser.controller.dto;

import com.rms.funds.holdings.analyser.model.IdName;
import lombok.*;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class HoldingChangeMetricDto {

    private Map<String, List<HoldingChangeMetricDto.StockPerformanceChange>> stockPerformance;

    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class StockPerformanceChange {

        private Long id;
        private String name;
        private String symbol;
        private IdName industry;
        private IdName sector;

        private double exposureChange;     // ₹ change
        private double relativeChangePct;  // % change in avg weight
        private int fundCountChange;       // fund breadth change
        private double score;              // composite score
    }
}
