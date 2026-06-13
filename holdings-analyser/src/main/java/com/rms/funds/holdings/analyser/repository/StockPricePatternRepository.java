package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.StockPricePatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockPricePatternRepository extends JpaRepository<StockPricePatternEntity, Long> {

    List<StockPricePatternEntity> findByStockId(Long stockId);

    List<StockPricePatternEntity> findByPatternType(String patternType);

    List<StockPricePatternEntity> findByStockIdAndPatternType(Long stockId, String patternType);

    List<StockPricePatternEntity> findByDateFromLessThanEqualAndDateToGreaterThanEqual(LocalDate start, LocalDate end);

    @Query(value = """
            SELECT * FROM stock_price_patterns 
            WHERE stock_id = :stockId
            ORDER BY pattern_embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<StockPricePatternEntity> findSimilarPatterns(
            @Param("stockId") Long stockId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    List<StockPricePatternEntity> findByConfidenceScoreGreaterThanOrderByConfidenceScoreDesc(Double threshold);

    List<StockPricePatternEntity> findByStockIdOrderByDateFromDesc(Long stockId);
}

