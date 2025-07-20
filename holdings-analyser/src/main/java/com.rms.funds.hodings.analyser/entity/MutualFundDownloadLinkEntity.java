package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MUTUAL_FUND_DOWNLOAD_LINKS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MutualFundDownloadLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MUTUAL_FUND_ID")
    private Long mutualFundId;

    @OneToOne
    //@PrimaryKeyJoinColumn
    @JoinColumn(name = "MUTUAL_FUND_ID", insertable = false, updatable = false)
    private MutualFundEntity mutualFund;

    private String url;

    private LocalDate atDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
