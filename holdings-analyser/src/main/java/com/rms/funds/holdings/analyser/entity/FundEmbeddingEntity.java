package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_embeddings")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FundEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_id", nullable = false, unique = true)
    private Long fundId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", insertable = false, updatable = false)
    private MutualFundEntity fund;

    @Column(name = "fund_name", nullable = false, length = 255)
    private String fundName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "fund_type", length = 100)
    private String fundType;

    @Column(name = "holdings_summary", columnDefinition = "TEXT")
    private String holdingsSummary;

    @Column(name = "embedding", columnDefinition = "vector(1536)", nullable = false)
    private float[] embedding;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

