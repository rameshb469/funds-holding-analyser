package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "STOCK_DETAILS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StockInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    @Column(name = "NAME_OF_COMPANY")
    private String company;
    private String series;
    @Column(name = "DATE_OF_LISTING")
    private Date dateOfListing;

    @Column(name = "PAID_UP_VALUE")
    private Integer paidUpValue;

    @Column(name = "MARKET_LOT")
    private Integer marketLot;

    @Column(name = "FACE_VALUE")
    private Integer faceValue;

    @OneToOne
    @JoinColumn(name = "industry_id", insertable = false, updatable = false)
    private IndustryEntity industry;

    @OneToOne
    @JoinColumn(name = "sector_id", insertable = false, updatable = false)
    private SectorEntity sector;

    @Column(name = "ISIN_NUMBER")
    private String isinNumber;

    @Column(name = "MKT_CAP")
    private Long marketCap;

//    @Column(name = "TOTAL_FLOATING_SHARES")
//    private Long totalFloatingShares;

    @Column(name = "shares_outstanding")
    private Long  sharesOutstanding;

    private Integer rank;

    private String marketCapCategory;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
