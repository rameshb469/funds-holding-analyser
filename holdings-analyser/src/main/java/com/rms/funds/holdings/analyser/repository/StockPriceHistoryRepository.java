package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory> findByStockIdAndTradeDateBetween(Long stockId, LocalDate start, LocalDate end);
    StockPriceHistory findTopByStockIdOrderByTradeDateDesc(Long stockId);
    boolean existsByStockIdAndTradeDate(Long stockId, LocalDate tradeDate);

    boolean existsByStockAndTradeDate(StockInfoEntity stock, LocalDate tradeDate);

    // Returns the latest history entry for the stock BEFORE the given date
    StockPriceHistory findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(Long stockId, LocalDate date);

    // Fetch all histories for a specific trade date
    List<StockPriceHistory> findByTradeDate(LocalDate tradeDate);

    // Fetch all histories within an inclusive date range
    List<StockPriceHistory> findByTradeDateBetween(LocalDate start, LocalDate end);
}
