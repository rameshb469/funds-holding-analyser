package com.rms.funds.holdings.analyser.controller.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderAgentStatusDto {

    private boolean running;
    private boolean enabled;
    private boolean liveMode;
    private LocalDate nextEligibleDate;
    private LocalDate todayIst;
    private OrderAgentCycleDto lastCycle;
    private long openPositions;
    private double todayRealisedPnl;
}
