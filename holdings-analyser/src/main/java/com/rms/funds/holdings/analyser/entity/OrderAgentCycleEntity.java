package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_agent_cycle")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderAgentCycleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_anchor_date", nullable = false)
    private LocalDate cycleAnchorDate;

    @Column(name = "target_trade_date", nullable = false)
    private LocalDate targetTradeDate;

    @Column(name = "actual_trade_date")
    private LocalDate actualTradeDate;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "skip_reason", length = 256)
    private String skipReason;

    @Column(name = "picks_json", length = 4000)
    private String picksJson;

    @Column(name = "broker_order_ids", length = 512)
    private String brokerOrderIds;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "realised_pnl")
    private Double realisedPnl;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
