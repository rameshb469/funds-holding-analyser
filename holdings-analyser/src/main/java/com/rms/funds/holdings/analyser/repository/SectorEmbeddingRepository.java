package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.SectorEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorEmbeddingRepository extends JpaRepository<SectorEmbeddingEntity, Long> {

    Optional<SectorEmbeddingEntity> findBySectorId(Long sectorId);

    Optional<SectorEmbeddingEntity> findBySectorName(String sectorName);

    @Query(value = """
            SELECT * FROM sector_embeddings 
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<SectorEmbeddingEntity> findSimilarSectors(@Param("embedding") String embedding, @Param("limit") int limit);
}

