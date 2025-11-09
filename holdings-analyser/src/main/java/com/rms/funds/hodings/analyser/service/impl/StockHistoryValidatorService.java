package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.entity.StockPriceHistory;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockHistoryValidatorService {

    private final StockPriceHistoryRepository historyRepository;
    private final StockInfoRepository stockInfoRepository;

    /**
     * Validate that each EQ stock present on the given trade date has a previous trading-day record.
     * If date is null, defaults to yesterday.
     */
    public List<StockHistoryValidationResult> validatePrevious(LocalDate date) {
        LocalDate tradeDate = (date != null) ? date : LocalDate.now().minusDays(1);

        List<StockPriceHistory> todays = historyRepository.findByTradeDate(tradeDate);
        List<StockHistoryValidationResult> results = new ArrayList<>();
        if (todays == null || todays.isEmpty()) return results;

        for (StockPriceHistory cur : todays) {
            // only EQ segments
            if (cur.getSecuritySeries() == null || !"EQ".equalsIgnoreCase(cur.getSecuritySeries())) continue;

            StockInfoEntity stock = cur.getStock();
            Long stockId = null;
            String symbol = cur.getTickerSymbol();
            String isin = cur.getIsin();

            if (stock != null) stockId = stock.getId();

            // attempt to lookup stock if not set on entity
            if (stockId == null) {
                Optional<StockInfoEntity> byIsin = Optional.empty();
                if (isin != null && !isin.trim().isEmpty()) byIsin = stockInfoRepository.findByIsinNumber(isin.trim());
                if (byIsin.isPresent()) {
                    stockId = byIsin.get().getId();
                    stock = byIsin.get();
                } else if (symbol != null && !symbol.trim().isEmpty()) {
                    Optional<StockInfoEntity> bySym = stockInfoRepository.findBySymbol(symbol.trim());
                    if (bySym.isPresent()) {
                        stockId = bySym.get().getId();
                        stock = bySym.get();
                    }
                }
            }

            StockHistoryValidationResult r = new StockHistoryValidationResult();
            r.setSymbol(symbol);
            r.setIsin(isin);

            if (stockId == null) {
                r.setStockId(null);
                r.setHasPrevious(false);
                r.setNote("Stock not found in master list (by ISIN or symbol)");
                results.add(r);
                continue;
            }

            r.setStockId(stockId);

            // find the latest history before tradeDate
            StockPriceHistory prev = historyRepository.findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(stockId, tradeDate);
            if (prev != null) {
                r.setHasPrevious(true);
                r.setPreviousTradeDate(prev.getTradeDate());
                r.setNote("Previous record found");
            } else {
                r.setHasPrevious(false);
                r.setNote("No previous trading-day record found");
            }
            results.add(r);
        }

        return results;
    }
}

