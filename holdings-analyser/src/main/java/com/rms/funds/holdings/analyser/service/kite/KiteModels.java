package com.rms.funds.holdings.analyser.service.kite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class KiteModels {

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteTokenResponse {
        private String accessToken;
        private String userId;
        private String userName;
        private String loginTime;
        private String publicToken;
        private String apiKey;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteOrderResponse {
        private String orderId;
        private String status;
        private String raw;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteApiError {
        private String status;
        private String message;
        private String errorType;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteOrderBook {
        private List<KiteOrderEntry> orders;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteOrderEntry {
        private String orderId;
        private String exchange;
        private String tradingSymbol;
        private String transactionType;
        private String orderType;
        private String product;
        private Integer quantity;
        private Double price;
        private Double triggerPrice;
        private String status;
        private String orderTimestamp;
        private String tag;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitePosition {
        private String exchange;
        private String tradingSymbol;
        private Integer quantity;
        private Integer overnightQuantity;
        private Integer multiplier;
        private Double averagePrice;
        private Double lastPrice;
        private Double pnl;
        private Integer buyQuantity;
        private Integer sellQuantity;
        private Integer dayBuyQuantity;
        private Integer daySellQuantity;
    }
}
