package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.StockEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockEmbeddingRepository extends JpaRepository<StockEmbeddingEntity, Long> {

    Optional<StockEmbeddingEntity> findByStockId(Long stockId);

    Optional<StockEmbeddingEntity> findBySymbol(String symbol);

    @Query(value = """
            SELECT * FROM stock_embeddings 
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<StockEmbeddingEntity> findSimilarStocks(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM stock_embeddings 
            WHERE sector = :sector
            ORDER BY embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<StockEmbeddingEntity> findSimilarStocksBySector(
            @Param("embedding") String embedding,
            @Param("sector") String sector,
            @Param("limit") int limit);

    List<StockEmbeddingEntity> findBySector(String sector);

    List<StockEmbeddingEntity> findByIndustry(String industry);
}

