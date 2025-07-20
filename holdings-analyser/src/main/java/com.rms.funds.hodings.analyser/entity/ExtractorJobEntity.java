package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "MF_HOLDING_EXTRACTOR_JOB_HISTORY")
public class ExtractorJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mutual_fund_config_id")
    private Long mutualFundConfigId;

    @OneToOne
    @JoinColumn(name = "mutual_fund_config_id", insertable = false, updatable = false)
    private MutualFundConfigEntity config;

    @Column(name = "URL")
    private String url;

    private LocalDate atDate;

    private String status;

    private String error;

    @Builder.Default
    @Column(name = "record_count")
    private int recordCount = 0;

    @Builder.Default
    @Column(name = "retry_count")
    private int retryCount = 0;

    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
