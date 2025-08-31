package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "mutual_fund_holding")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MfHoldingEntity {

    @Id
    private Long id;

    @Column(name = "MUTUAL_FUND_ID")
    private Long mfId;

    @OneToOne
    @JoinColumn(name = "MUTUAL_FUND_ID", insertable = false, updatable = false)
    private MutualFundEntity mfEntity;

    @Column(name = "stock_id")
    private Long stockId;

    @OneToOne
    @JoinColumn(name = "stock_id", insertable = false, updatable = false)
    private StockInfoEntity stockInfoEntity;

    private Long quantity;

    private Double marketValue;

    private Double avgPrice;

//    @Column(name = "net_asset_pct")
//    private Double netAssetPct;

    private LocalDate atDate;

    public Double getAvgPrice() {
        if (quantity != null && quantity > 0) {
            return (marketValue/quantity);
        }
        return 0.0;
    }
}
