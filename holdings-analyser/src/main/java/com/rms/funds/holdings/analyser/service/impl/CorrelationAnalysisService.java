package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorrelationAnalysisService {

    private final StockInfoRepository stockInfoRepository;
    private final StockPriceHistoryRepository historyRepository;

    // Minimum overlapping return observations to compute correlation
    private static final int MIN_OVERLAP = 10;

    public List<CorrelationResult> computeTopCorrelations(LocalDate start, LocalDate end, int topN) {
        List<StockInfoEntity> stocks = stockInfoRepository.findAll();

        // Build returns map for each stock: stockId -> Map<tradeDate, dailyReturn>
        Map<Long, Map<LocalDate, Double>> returnsByStock = new HashMap<>();

        for (StockInfoEntity stock : stocks) {
            Long sid = stock.getId();
            if (sid == null) continue;
            List<StockPriceHistory> histories = historyRepository.findByStockIdAndTradeDateBetween(sid, start, end);
            if (histories == null || histories.size() < 2) continue;
            // sort by tradeDate
            histories.sort(Comparator.comparing(StockPriceHistory::getTradeDate));
            Map<LocalDate, Double> returns = new HashMap<>();
            StockPriceHistory prev = null;
            for (StockPriceHistory cur : histories) {
                if (prev != null && prev.getClosePrice() != null && cur.getClosePrice() != null
                        && prev.getClosePrice().doubleValue() != 0.0) {
                    double r = (cur.getClosePrice().doubleValue() - prev.getClosePrice().doubleValue()) / prev.getClosePrice().doubleValue();
                    returns.put(cur.getTradeDate(), r);
                }
                prev = cur;
            }
            if (returns.size() >= MIN_OVERLAP) returnsByStock.put(sid, returns);
        }

        List<Long> stockIds = new ArrayList<>(returnsByStock.keySet());
        List<CorrelationResult> results = new ArrayList<>();

        for (int i = 0; i < stockIds.size(); i++) {
            for (int j = i + 1; j < stockIds.size(); j++) {
                Long s1 = stockIds.get(i);
                Long s2 = stockIds.get(j);
                Map<LocalDate, Double> r1 = returnsByStock.get(s1);
                Map<LocalDate, Double> r2 = returnsByStock.get(s2);
                // intersect dates
                Set<LocalDate> common = new HashSet<>(r1.keySet());
                common.retainAll(r2.keySet());
                if (common.size() < MIN_OVERLAP) continue;
                double[] a = new double[common.size()];
                double[] b = new double[common.size()];
                int idx = 0;
                for (LocalDate d : common) {
                    a[idx] = r1.get(d);
                    b[idx] = r2.get(d);
                    idx++;
                }
                double corr = pearson(a, b);
                StockInfoEntity stock1 = stockInfoRepository.findById(s1).orElse(null);
                StockInfoEntity stock2 = stockInfoRepository.findById(s2).orElse(null);
                String sym1 = stock1 != null ? stock1.getSymbol() : "#" + s1;
                String sym2 = stock2 != null ? stock2.getSymbol() : "#" + s2;
                CorrelationResult cr = new CorrelationResult(
                        s1, s2, sym1, sym2, corr, common.size()
                );
                results.add(cr);
            }
        }

        // sort by absolute correlation desc to get strongest relationships
        results.sort(Comparator.comparingDouble((CorrelationResult r) -> Math.abs(r.getCorrelation())).reversed());

        return results.stream().limit(topN).collect(Collectors.toList());
    }

    private double pearson(double[] x, double[] y) {
        if (x.length != y.length || x.length == 0) return 0.0;
        int n = x.length;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
        }
        double meanX = sumX / n;
        double meanY = sumY / n;
        double num = 0, denX = 0, denY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            num += dx * dy;
            denX += dx * dx;
            denY += dy * dy;
        }
        double den = Math.sqrt(denX * denY);
        if (den == 0) return 0.0;
        return num / den;
    }
}
