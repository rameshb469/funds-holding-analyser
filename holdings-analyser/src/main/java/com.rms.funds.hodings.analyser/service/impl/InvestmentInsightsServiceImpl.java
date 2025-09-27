package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.controller.dto.InvestmentInsightsResponse;
import com.rms.funds.hodings.analyser.controller.dto.StockInsight;
import com.rms.funds.hodings.analyser.repository.MfHoldingRepository;
import com.rms.funds.hodings.analyser.service.InvestmentInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentInsightsServiceImpl implements InvestmentInsightsService {

    private final MfHoldingRepository holdingRepository;
    private static final double STABLE_WEIGHT_DELTA_PCT = 0.25;

    @Override
    public InvestmentInsightsResponse getInsights(LocalDate asOfDate) {
        // 1. Determine latest date if null
        if (asOfDate == null) {
            asOfDate = holdingRepository.findAll().stream()
                    .max(Comparator.comparing(MfHoldingEntity::getAtDate))
                    .map(MfHoldingEntity::getAtDate)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to fetch last date reports"));
        }

        // 2. Load last 6 months of data
        LocalDate sixMonthsAgo = asOfDate.minusMonths(3).withDayOfMonth(1);
        List<MfHoldingEntity> last6Months = holdingRepository.findByAtDateBetween(sixMonthsAgo, asOfDate);

        // 3. Group holdings by stock
        Map<Long, List<MfHoldingEntity>> stockHoldingsMap = last6Months.stream()
                .collect(Collectors.groupingBy(MfHoldingEntity::getStockId));

        List<StockInsight> insights = new ArrayList<>();

        for (Map.Entry<Long, List<MfHoldingEntity>> entry : stockHoldingsMap.entrySet()) {
            Long stockId = entry.getKey();
            List<MfHoldingEntity> stockHoldings = entry.getValue();

            // Group by fund
            Map<Long, List<MfHoldingEntity>> fundTimeline = stockHoldings.stream()
                    .collect(Collectors.groupingBy(MfHoldingEntity::getMfId));

            // Aggregate latest month for metrics
            MfHoldingEntity latest = stockHoldings.stream()
                    .max(Comparator.comparing(MfHoldingEntity::getAtDate)).orElse(null);
            if (latest == null || latest.getStockInfoEntity() == null) continue;

            StockInfoEntity s = latest.getStockInfoEntity();

            // ✅ Skip stocks with null marketCapCategory
            if (s.getMarketCapCategory() == null)  continue;

            // Compute prev month (or earliest available) metrics per fund
            List<Long> fundIncreasedCount = new ArrayList<>();
            int totalFunds = fundTimeline.size();

            double netAssetCurr = 0.0;
            long quantityCurr = 0L;
            double valueCurr = 0.0;
            double netAssetPrev = 0.0;
            long quantityPrev = 0L;
            double valuePrev = 0.0;

            // For trend-based metrics
            int trimmedMonths = 0;
            boolean newlyAddedFlag = false;
            boolean exitedFlag = false;

            for (Map.Entry<Long, List<MfHoldingEntity>> fundEntry : fundTimeline.entrySet()) {
                List<MfHoldingEntity> timeline = fundEntry.getValue();
                timeline.sort(Comparator.comparing(MfHoldingEntity::getAtDate));

                MfHoldingEntity first = timeline.get(0);
                MfHoldingEntity last = timeline.get(timeline.size() - 1);

                long prevQty = first.getQuantity() != null ? first.getQuantity() : 0L;
                long currQty = last.getQuantity() != null ? last.getQuantity() : 0L;
                double prevVal = first.getMarketValue() != null ? first.getMarketValue() : 0.0;
                double currVal = last.getMarketValue() != null ? last.getMarketValue() : 0.0;
                double prevNet = first.getNetAssetPct() != null ? first.getNetAssetPct() : 0.0;
                double currNet = last.getNetAssetPct() != null ? last.getNetAssetPct() : 0.0;

                quantityCurr += currQty;
                quantityPrev += prevQty;
                valueCurr += currVal;
                valuePrev += prevVal;
                netAssetCurr += currNet;
                netAssetPrev += prevNet;

                // Count funds increased for Consensus Buy
                if (currQty > prevQty) fundIncreasedCount.add(fundEntry.getKey());

                // Newly Added / Exit
                if (prevQty == 0 && currQty > 0) newlyAddedFlag = true;
                if (currQty == 0 && prevQty > 0) exitedFlag = true;

                // Trimmed
                if (timeline.size() >= 2) {
                    boolean decreasing = true;
                    for (int i = 1; i < timeline.size(); i++) {
                        long qPrev = timeline.get(i - 1).getQuantity() != null ? timeline.get(i - 1).getQuantity() : 0;
                        long qCurr = timeline.get(i).getQuantity() != null ? timeline.get(i).getQuantity() : 0;
                        if (qCurr >= qPrev) {
                            decreasing = false;
                            break;
                        }
                    }
                    if (decreasing) trimmedMonths = timeline.size();
                }
            }

            double weightChangePct = netAssetCurr - netAssetPrev;
            double quantityChangePct = quantityPrev > 0 ? ((double) (quantityCurr - quantityPrev) / quantityPrev) * 100.0 : 0.0;
            double valueChangePct = valuePrev > 0 ? ((valueCurr - valuePrev) / valuePrev) * 100.0 : 0.0;

            // Calculate signals
            List<String> signals = new ArrayList<>();
            int score = 0;

            // Strong Exposure
            if (("LargeCap".equals(s.getMarketCapCategory()) && netAssetCurr >= 3.0)
                    || ("MidCap".equals(s.getMarketCapCategory()) && netAssetCurr >= 2.0)
                    || ("SmallCap".equals(s.getMarketCapCategory()) && netAssetCurr >= 1.0)) {
                signals.add("Strong Exposure");
                score += 3;
            }

            // Accumulation
            if (quantityChangePct >= 10.0) {
                signals.add("Accumulation");
                score += 2;
            }

            // Valuation Growth with Accumulation
            if (valueChangePct >= 15.0 && quantityChangePct > 0) {
                signals.add("Valuation Growth (with Accumulation)");
                score += 2;
            }

            // Newly Added
            if (newlyAddedFlag) {
                signals.add("Newly Added");
                score += 1;
            }

            // Trimmed Holding
            if (trimmedMonths >= 2) {
                signals.add("Trimmed Holding");
                score -= 2;
            }

            // Exit / Zero Holding
            if (exitedFlag) {
                signals.add("Exit / Zero Holding");
                score -= 3;
            }

            // Consensus Buy (>=50% of funds)
            if (!fundIncreasedCount.isEmpty() && ((double) fundIncreasedCount.size() / totalFunds) >= 0.5) {
                signals.add("Consensus Buy");
                switch (s.getMarketCapCategory()) {
                    case "SmallCap": score += 3; break;
                    case "MidCap": score += 2; break;
                    case "LargeCap": score += 1; break;
                }
            }

            // Build StockInsight
            StockInsight insight = StockInsight.builder()
                    .stockId(stockId)
                    .symbol(s.getSymbol())
                    .company(s.getCompany())
                    .marketCapCategory(s.getMarketCapCategory())
                    .quantityCurr(quantityCurr)
                    .quantityPrev(quantityPrev)
                    .valueCurr(valueCurr)
                    .valuePrev(valuePrev)
                    .netAssetPctCurr(netAssetCurr)
                    .netAssetPctPrev(netAssetPrev)
                    .weightChangePct(weightChangePct)
                    .quantityChangePct(quantityChangePct)
                    .valueChangePct(valueChangePct)
                    .signals(signals)
                    .score(score)
                    .build();

            insights.add(insight);
        }

        // Sort by score descending
        insights.sort(Comparator.comparing(StockInsight::getScore).reversed());

        return InvestmentInsightsResponse.builder()
                .currDate(asOfDate)
                .prevDate(asOfDate.minusMonths(1))
                .stocks(insights)
                .build();
    }
}
