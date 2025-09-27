package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfoEntity, Long> {

    Optional<StockInfoEntity> findByIsinNumber(String isinNumber);
    Optional<StockInfoEntity> findBySymbol(String symbol);

    // Fetch all stocks where marketCap is null
    List<StockInfoEntity> findByMarketCapCategoryIsNull();
}
