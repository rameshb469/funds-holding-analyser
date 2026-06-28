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

    /**
     * Subset of the {@code /quote} response. Depth is the 5-level bid/ask book
     * and is only populated on paid Kite Connect plans; on the free plan Kite
     * returns the body but with {@code depth} absent or null.
     */
    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteQuote {
        private String instrumentToken;
        private String tradingSymbol;
        private String exchange;
        private Double lastPrice;
        private Double change;
        private KiteOhlc ohlc;
        private KiteDepth depth;
        private Integer volume;
        private Double averagePrice;
        private Double lowerCircuitLimit;
        private Double upperCircuitLimit;
        private Double openInterest;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteOhlc {
        private Double open;
        private Double high;
        private Double low;
        private Double close;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteDepth {
        private List<KiteDepthLevel> buy;
        private List<KiteDepthLevel> sell;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteDepthLevel {
        private Double price;
        private Integer quantity;
        private Integer orders;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KiteQuoteEnvelope {
        /** Raw response text — used to surface the upstream message if parsing fails. */
        private String raw;
        /** Parsed quote if the response was well-formed. May be null on the free plan. */
        private KiteQuote quote;
        /** Set true when Kite returns a DataException / permission error indicating paid data is required. */
        private boolean paidDataRequired;
        /** Upstream error_type if any (e.g. "DataException", "PermissionDenied"). */
        private String errorType;
        /** Upstream message, if any. */
        private String message;
    }
}
