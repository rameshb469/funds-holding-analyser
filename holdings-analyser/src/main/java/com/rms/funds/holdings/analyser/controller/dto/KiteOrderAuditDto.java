package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KiteOrderAuditDto {

    private Long id;
    private Long stockId;
    private String symbol;
    private String exchange;
    private String transactionType;
    private String orderType;
    private String product;
    private Integer quantity;
    private Double price;
    private Double triggerPrice;
    private String tag;
    private String validity;
    private String kiteOrderId;
    private String status;
    private boolean sandbox;
    private LocalDateTime createdAt;
}
