package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
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

    // Return top N stocks by market cap descending; using Spring Data derived query
    List<StockInfoEntity> findTop1000ByOrderByMarketCapDesc();

    List<StockInfoEntity> findAllByOrderByMarketCapDesc();

    // For the Broker page symbol picker — keep payloads small by returning a bounded
    // alphabetical list (or a symbol prefix search). The frontend uses these to
    // populate a combobox that ultimately posts the row's `symbol` column value.
    List<StockInfoEntity> findTop500ByOrderBySymbolAsc();

    List<StockInfoEntity> findTop20BySymbolContainingIgnoreCaseOrderBySymbolAsc(String symbol);
}
