package com.rms.funds.holdings.analyser.schedulers;

import com.rms.funds.holdings.analyser.config.AgentConfigProperties;
import com.rms.funds.holdings.analyser.service.agent.OrderExecutionAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives the order-execution agent. Fires every weekday at 09:00 IST; the agent
 * itself decides whether today is an eligible cycle day and is a no-op otherwise.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExecutionAgentScheduler {

    private final OrderExecutionAgentService agent;
    private final AgentConfigProperties agentProps;

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Kolkata")
    public void weekdayTick() {
        if (!agentProps.isEnabled()) {
            return;
        }
        try {
            agent.dailyTick();
        } catch (Exception ex) {
            log.error("Order agent daily tick failed: {}", ex.getMessage(), ex);
        }
    }
}
