package com.rms.funds.hodings.analyser.controller.dto;

import com.rms.funds.hodings.analyser.model.IdName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockHoldingDto {

    private Long stockId;
    private String stockName;
    private String symbol;
    private IdName sector;
    private IdName industry;
    private long totalQuantity;
    private double quantityChange;
    private double valuation;
    private double valuationChange;
    private double avgPrice;
    private double avgPriceChange;
    private int totalMfCount;
    private int totalMfCountChange;
    @Builder.Default
    private List<Pair<LocalDate,List<MFQuantity>>> quantityChangeByDates = new ArrayList<>();
    @Builder.Default
    private Map<LocalDate, List<MFQuantity>> mfDistribution = new LinkedHashMap<>();
    @Builder.Default
    private List<Pair<LocalDate, Double>> valuationChangeByDates = new ArrayList<>();

    @Builder(toBuilder = true)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MFQuantity {
        private Long mfId;
        private String mfName;
        private Long quantity;
    }
}
