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
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_agent_position")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderAgentPositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "exchange", nullable = false, length = 16)
    private String exchange;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "entry_price", nullable = false)
    private Double entryPrice;

    @Column(name = "entry_order_id", length = 64)
    private String entryOrderId;

    @Column(name = "stop_loss_order_id", length = 64)
    private String stopLossOrderId;

    @Column(name = "trail_high", nullable = false)
    private Double trailHigh;

    @Column(name = "ladder_level", nullable = false)
    private Short ladderLevel;

    @Column(name = "ladder_exit_order_ids", length = 512)
    private String ladderExitOrderIds;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "close_price")
    private Double closePrice;

    @Column(name = "realised_pnl")
    private Double realisedPnl;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
