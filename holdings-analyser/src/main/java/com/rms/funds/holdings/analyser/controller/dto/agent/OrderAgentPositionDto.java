package com.rms.funds.holdings.analyser.controller.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderAgentPositionDto {

    private Long id;
    private Long cycleId;
    private Long stockId;
    private String symbol;
    private String exchange;
    private Integer qty;
    private Double entryPrice;
    private Double trailHigh;
    private Integer ladderLevel;
    private String stopLossOrderId;
    private String status;
    private Double closePrice;
    private Double realisedPnl;
    private LocalDateTime closedAt;
}
