package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KiteOrderDto {

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
