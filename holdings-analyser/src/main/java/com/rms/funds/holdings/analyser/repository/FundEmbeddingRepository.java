package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.FundEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundEmbeddingRepository extends JpaRepository<FundEmbeddingEntity, Long> {

    Optional<FundEmbeddingEntity> findByFundId(Long fundId);

    Optional<FundEmbeddingEntity> findByFundName(String fundName);

    @Query(value = """
            SELECT * FROM fund_embeddings 
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<FundEmbeddingEntity> findSimilarFunds(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM fund_embeddings 
            WHERE fund_type = :fundType
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<FundEmbeddingEntity> findSimilarFundsByType(
            @Param("embedding") String embedding,
            @Param("fundType") String fundType,
            @Param("limit") int limit);

    List<FundEmbeddingEntity> findByFundType(String fundType);
}

