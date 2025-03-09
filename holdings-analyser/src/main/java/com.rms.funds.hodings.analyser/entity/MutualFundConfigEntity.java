package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_fund_config")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MutualFundConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long mutualFundId;

    @OneToOne
    @PrimaryKeyJoinColumn
    @JoinColumn(name = "mutual_fund_id", insertable = false, updatable = false)
    private MutualFundEntity mutualFund;

    @Column(name = "DOWNLOAD_URL")
    private String downloadUrl;

    @Column(name = "IS_ZIPPED")
    private boolean isZipped;

    private String sheetName;

    private String contentType;

    private String extension;

    @Column(name = "ISIN_COL_MAPPER")
    private Integer isinCodeColNumber;

    @Column(name = "STOCK_COL_MAPPER")
    private Integer stockNameColNumber;

    @Column(name = "INDUSTRY_COL_MAPPER")
    private Integer industryCodeColumnNumber;

    @Column(name = "QUANTITY_COL_MAPPER")
    private Integer quantityColNumber;

    @Column(name = "MARKET_VALUE_COL_MAPPER")
    private Integer marketValueColNumber;

    @Column(name = "NET_ASSET_PERC_COL_MAPPER")
    private Integer netAssetPercColNumber;

    @Column(name = "IS_PICK_BY_SYSTEM")
    private boolean isPickValuesBySystem;

    @Column(name = "DATE_MAPPER_1")
    private String dateMapper1;

    @Column(name = "DATE_MAPPER_1_CONFIG")
    private String dateMapper1Config;

    @Column(name = "DATE_MAPPER_2")
    private String dateMapper2;

    @Column(name = "DATE_MAPPER_2_CONFIG")
    private String dateMapper2Config;

    @Column(name = "DATE_MAPPER_3")
    private String dateMapper3;

    @Column(name = "DATE_MAPPER_3_CONFIG")
    private String dateMapper3Config;

    @Column(name = "DATE_MAPPER_4")
    private String dateMapper4;

    @Column(name = "DATE_MAPPER_4_CONFIG")
    private String dateMapper4Config;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
