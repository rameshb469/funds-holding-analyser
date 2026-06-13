package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.InvestmentInsightEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvestmentInsightRepository extends JpaRepository<InvestmentInsightEntity, Long> {

    List<InvestmentInsightEntity> findByFundId(Long fundId);

    List<InvestmentInsightEntity> findByInsightType(String insightType);

    List<InvestmentInsightEntity> findByFundIdAndInsightType(Long fundId, String insightType);

    @Query(value = """
            SELECT * FROM investment_insights 
            WHERE fund_id = :fundId
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<InvestmentInsightEntity> findSimilarInsights(
            @Param("fundId") Long fundId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    List<InvestmentInsightEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT i FROM InvestmentInsightEntity i WHERE i.fundId = :fundId ORDER BY i.createdAt DESC")
    List<InvestmentInsightEntity> findLatestInsights(@Param("fundId") Long fundId, Pageable pageable);

    List<InvestmentInsightEntity> findByConfidenceScoreGreaterThanOrderByConfidenceScoreDesc(Double threshold);
}

