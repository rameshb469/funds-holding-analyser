package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KiteQuoteDto {
    private String instrumentToken;
    private String tradingSymbol;
    private String exchange;
    private Double lastPrice;
    private Double change;
    private KiteOhlcDto ohlc;
    private KiteDepthDto depth;
    private Integer volume;
    private Double averagePrice;
    private boolean paidDataRequired;
    private String errorType;
    private String message;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KiteOhlcDto {
        private Double open;
        private Double high;
        private Double low;
        private Double close;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KiteDepthDto {
        private List<KiteDepthLevelDto> buy;
        private List<KiteDepthLevelDto> sell;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KiteDepthLevelDto {
        private Double price;
        private Integer quantity;
        private Integer orders;
    }
}
