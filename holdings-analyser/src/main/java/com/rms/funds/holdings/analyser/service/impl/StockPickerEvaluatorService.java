package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockPickerEvaluatorService {

    private final StockPriceHistoryRepository historyRepository;
    private final StockInfoRepository stockInfoRepository;

    public static class EvaluationSummary {
        public int totalPicks;
        public int evaluated;
        public int successes;
        public double successRate; // percentage 0-100
        public double avgReturnPct;
        public double medianReturnPct;
        public List<PickDetail> picks = new ArrayList<>();
    }

    public static class PickDetail {
        public Long stockId;
        public String symbol;
        public String isin;
        public BigDecimal entryClose;
        public BigDecimal pastClose;
        public double pickPct;
        public BigDecimal futureClose;
        public Double futureReturnPct;
        public boolean success;
        public LocalDate entryDate;
        public LocalDate futureDate;
    }

    /**
     * Evaluate picking topN stocks by percent change from a past date (lookbackDays) to tradeDate, then hold for holdDays.
     * lookbackDays: number of calendar days to look back (best-effort to find a historic price near that date). Default 1.
     * holdDays: number of calendar days after tradeDate to evaluate performance. Default 1.
     */
    public EvaluationSummary evaluate(LocalDate tradeDate, int lookbackDays, int topN, int holdDays) {
        if (tradeDate == null) tradeDate = LocalDate.now().minusDays(1);
        if (lookbackDays < 1) lookbackDays = 1;
        if (topN < 1) topN = 10;
        if (holdDays < 1) holdDays = 1;

        // load today's records
        List<StockPriceHistory> todays = historyRepository.findByTradeDate(tradeDate);
        if (todays == null) todays = Collections.emptyList();

        // build candidates with entryClose and pastClose
        List<PickDetail> candidates = new ArrayList<>();

        for (StockPriceHistory cur : todays) {
            try {
                if (cur.getSecuritySeries() == null || !"EQ".equalsIgnoreCase(cur.getSecuritySeries())) continue;
                Long stockId = (cur.getStock() != null) ? cur.getStock().getId() : null;
                String symbol = cur.getTickerSymbol() != null ? cur.getTickerSymbol().trim() : null;
                String isin = cur.getIsin() != null ? cur.getIsin().trim() : null;

                // Resolve StockInfoEntity early so we can filter by marketCapCategory (only LARGECAP)
                StockInfoEntity stock = cur.getStock();
                if (stock == null) {
                    // try to find master by ISIN or symbol
                    if (isin != null && !isin.trim().isEmpty()) {
                        Optional<StockInfoEntity> byIsin = stockInfoRepository.findByIsinNumber(isin.trim());
                        if (byIsin.isPresent()) {
                            stock = byIsin.get();
                            stockId = stock.getId();
                        }
                    }
                    if (stock == null && symbol != null && !symbol.trim().isEmpty()) {
                        Optional<StockInfoEntity> bySym = stockInfoRepository.findBySymbol(symbol.trim());
                        if (bySym.isPresent()) {
                            stock = bySym.get();
                            stockId = stock.getId();
                        }
                    }
                }

                // Only pick largecap stocks
                if (stock == null) continue; // skip if master not found
                String mcapCat = stock.getMarketCapCategory();
                if (mcapCat == null || !"LARGECAP".equalsIgnoreCase(mcapCat.trim())) continue;
                BigDecimal entryClose = cur.getClosePrice();
                if (entryClose == null) continue;

                // find a historic price near tradeDate - lookbackDays
                LocalDate target = tradeDate.minusDays(lookbackDays);
                LocalDate searchStart = target.minusDays(7);
                LocalDate searchEnd = tradeDate.minusDays(1);
                BigDecimal pastClose = null;

                if (stockId != null) {
                    List<StockPriceHistory> prevs = historyRepository.findByStockIdAndTradeDateBetween(stockId, searchStart, searchEnd);
                    if (prevs != null && !prevs.isEmpty()) {
                        // sort by trade date
                        prevs.sort(Comparator.comparing(StockPriceHistory::getTradeDate));
                        // try to find exact closest record <= target first
                        StockPriceHistory chosen = null;
                        long bestDiff = Long.MAX_VALUE;
                        for (StockPriceHistory p : prevs) {
                            long diff = Math.abs(p.getTradeDate().toEpochDay() - target.toEpochDay());
                            if (diff < bestDiff) {
                                bestDiff = diff;
                                chosen = p;
                            }
                        }
                        if (chosen != null) {
                            pastClose = chosen.getClosePrice();
                        }

                        // If exact/closest isn't ideal and we have both a record before and after target, interpolate
                        StockPriceHistory before = null, after = null;
                        for (StockPriceHistory p : prevs) {
                            if (!p.getTradeDate().isAfter(target)) before = p;
                            if (p.getTradeDate().isAfter(target)) { after = p; break; }
                        }
                        if (pastClose == null && before != null && after != null) {
                            // linear interpolate close price at target date
                            long totalDays = after.getTradeDate().toEpochDay() - before.getTradeDate().toEpochDay();
                            long daysFromBefore = target.toEpochDay() - before.getTradeDate().toEpochDay();
                            if (totalDays > 0) {
                                try {
                                    double beforeVal = before.getClosePrice() != null ? before.getClosePrice().doubleValue() : Double.NaN;
                                    double afterVal = after.getClosePrice() != null ? after.getClosePrice().doubleValue() : Double.NaN;
                                    if (!Double.isNaN(beforeVal) && !Double.isNaN(afterVal)) {
                                        double interp = beforeVal + (afterVal - beforeVal) * ((double) daysFromBefore / (double) totalDays);
                                        pastClose = BigDecimal.valueOf(interp);
                                    }
                                } catch (Exception e) {
                                    // ignore interpolation errors
                                }
                            }
                        }
                    }
                    // if not found, try immediate previous
                    if (pastClose == null) {
                        StockPriceHistory prev = historyRepository.findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(stockId, tradeDate);
                        if (prev != null) {
                            pastClose = prev.getClosePrice();
                        }
                    }
                }

                // if we couldn't find pastClose by id, try lookup by isin or symbol in db history
                if (pastClose == null) {
                    // try find stockId via master
                    if (stockId == null) {
                        Optional<StockInfoEntity> byIsin = Optional.empty();
                        if (isin != null && !isin.trim().isEmpty()) byIsin = stockInfoRepository.findByIsinNumber(isin.trim());
                        if (byIsin.isPresent()) stockId = byIsin.get().getId();
                        else if (symbol != null && !symbol.trim().isEmpty()) {
                            Optional<StockInfoEntity> bySym = stockInfoRepository.findBySymbol(symbol.trim());
                            if (bySym.isPresent()) stockId = bySym.get().getId();
                        }
                    }
                    if (stockId != null) {
                        StockPriceHistory prev = historyRepository.findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(stockId, tradeDate);
                        if (prev != null) {
                            pastClose = prev.getClosePrice();
                        }
                    }
                }

                if (pastClose == null) continue; // cannot compute pick metric

                double pct;
                try {
                    pct = entryClose.subtract(pastClose)
                            .divide(pastClose, 6, RoundingMode.HALF_UP)
                            .doubleValue() * 100.0;
                } catch (Exception e) {
                    pct = 0.0;
                }

                PickDetail pd = new PickDetail();
                pd.stockId = stockId;
                pd.symbol = symbol;
                pd.isin = isin;
                pd.entryClose = entryClose;
                pd.pastClose = pastClose;
                pd.pickPct = pct;
                pd.entryDate = tradeDate;
                candidates.add(pd);

            } catch (Exception ex) {
                // ignore row
            }
        }

        // sort by pickPct desc and take topN
        candidates.sort(Comparator.comparingDouble((PickDetail p) -> p.pickPct).reversed());
        List<PickDetail> picks = candidates.stream().limit(topN).toList();

        EvaluationSummary summary = new EvaluationSummary();
        summary.totalPicks = picks.size();

        int evaluated = 0;
        int successes = 0;
        List<Double> returns = new ArrayList<>();

        for (PickDetail p : picks) {
            // find future price at or after tradeDate+holdDays (search window)
            LocalDate futureStart = tradeDate.plusDays(holdDays);
            LocalDate futureEnd = tradeDate.plusDays(holdDays + 7);
            BigDecimal futureClose = null;
            LocalDate futureDate = null;
            if (p.stockId != null) {
                List<StockPriceHistory> futs = historyRepository.findByStockIdAndTradeDateBetween(p.stockId, futureStart, futureEnd);
                if (futs != null && !futs.isEmpty()) {
                    // pick earliest
                    futs.sort(Comparator.comparing(StockPriceHistory::getTradeDate));
                    StockPriceHistory f = futs.get(0);
                    futureClose = f.getClosePrice();
                    futureDate = f.getTradeDate();
                }
            }
            if (futureClose != null && p.entryClose != null && p.entryClose.compareTo(BigDecimal.ZERO) != 0) {
                evaluated++;
                double futRet;
                try {
                    futRet = futureClose.subtract(p.entryClose)
                            .divide(p.entryClose, 6, RoundingMode.HALF_UP)
                            .doubleValue() * 100.0;
                } catch (Exception ex) {
                    futRet = 0.0;
                }
                p.futureClose = futureClose;
                p.futureReturnPct = futRet;
                p.futureDate = futureDate;
                boolean success = futRet > 0; // simple definition: positive return
                p.success = success;
                if (success) successes++;
                returns.add(futRet);
            } else {
                p.futureClose = null;
                p.futureReturnPct = null;
                p.success = false;
            }
            summary.picks.add(p);
        }

        summary.evaluated = evaluated;
        summary.successes = successes;
        summary.successRate = (evaluated > 0) ? ((double) successes / evaluated) * 100.0 : 0.0;
        if (!returns.isEmpty()) {
            summary.avgReturnPct = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            Collections.sort(returns);
            summary.medianReturnPct = returns.get(returns.size() / 2);
        } else {
            summary.avgReturnPct = 0.0;
            summary.medianReturnPct = 0.0;
        }

        return summary;
    }
}
