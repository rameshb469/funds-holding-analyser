package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "MUTUAL_FUND_HOLDINGS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MutualFundHoldingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MUTUAL_FUND_ID")
    private Long mutualFundId;

    @OneToOne
    //@PrimaryKeyJoinColumn
    @JoinColumn(name = "MUTUAL_FUND_ID", insertable = false, updatable = false)
    private MutualFundEntity houseEntity;

    @Column(name = "STOCK_ID")
    private Long stockId;

    @OneToOne
    //@PrimaryKeyJoinColumn
    @JoinColumn(name = "STOCK_ID", insertable = false, updatable = false)
    private StockInfoEntity stockInfoEntity;

    private Long quantity;

    private Double marketValue;

    private Double avgPrice;

    private Double netAssetPct;

    private LocalDate atDate;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}
