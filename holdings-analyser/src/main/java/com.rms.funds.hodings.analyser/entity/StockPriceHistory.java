package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "trade_date"}))
@Data
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private StockInfoEntity stock;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    private Long volume;
    private BigDecimal turnover;
    private Long numberOfTrades;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // getters/setters
}

