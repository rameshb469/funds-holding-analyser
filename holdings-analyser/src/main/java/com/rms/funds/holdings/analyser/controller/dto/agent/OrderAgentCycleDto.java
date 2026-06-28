package com.rms.funds.holdings.analyser.controller.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderAgentCycleDto {

    private Long id;
    private LocalDate cycleAnchorDate;
    private LocalDate targetTradeDate;
    private LocalDate actualTradeDate;
    private String status;
    private String skipReason;
    private List<String> picks;
    private List<String> brokerOrderIds;
    private LocalDateTime closedAt;
    private Double realisedPnl;
    private LocalDateTime createdAt;
}
