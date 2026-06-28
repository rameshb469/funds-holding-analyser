package com.rms.funds.holdings.analyser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agent")
@Getter
@Setter
public class AgentConfigProperties {

    /** Total rupees deployed per cycle across all 5 picks. */
    private double capitalPerCycle = 100_000d;

    /** Hard floor: SL-M placed at entry * (1 - stopLossPct). */
    private double stopLossPct = 0.03d;

    /** First ladder rung above entry: entry * (1 + level1Pct). */
    private double ladderLevel1Pct = 0.05d;

    /** Second rung. */
    private double ladderLevel2Pct = 0.10d;

    /** Third rung. */
    private double ladderLevel3Pct = 0.15d;

    /** How much of the position to exit at each ladder rung (0.33 = one third per rung). */
    private double ladderExitFraction = 0.33d;

    /** Liquidate any OPEN position at 15:10 IST if it is still below the floor return. */
    private boolean eodLiquidateBelowFloor = true;

    /** Master switch — flips off the entire agent without removing the bean. */
    private boolean enabled = true;

    /** Number of picks per cycle. */
    private int picksPerCycle = 5;

    /** Polling cadence in milliseconds. Plan and tests both rely on this being 1000ms. */
    private long monitorIntervalMs = 1000L;
}
