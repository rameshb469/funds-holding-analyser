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
public class KiteOrderRequestDto {

    private Long stockId;
    private String tradingsymbol;
    private String exchange;
    private String transactionType;
    private String orderType;
    private String product;
    private Integer quantity;
    private Double price;
    private Double triggerPrice;
    private String tag;
    private String validity;
}
