package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.entity.StockPriceHistory;
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
}
