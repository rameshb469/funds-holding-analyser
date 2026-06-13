package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "sector_embeddings")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SectorEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sector_id", nullable = false, unique = true)
    private Long sectorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", insertable = false, updatable = false)
    private SectorEntity sector;

    @Column(name = "sector_name", nullable = false, length = 100)
    private String sectorName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics;

    @Column(name = "embedding", columnDefinition = "vector(1536)", nullable = false)
    private float[] embedding;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

