package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundHoldingEntity;
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
        if (asOfDate == null) {
            asOfDate = holdingRepository.findAll().stream().max(Comparator.comparing(MfHoldingEntity::getAtDate))
                    .map(MfHoldingEntity::getAtDate)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to fetch last date reports"));
        }
        LocalDate prevDate = asOfDate.minusMonths(1).withDayOfMonth(asOfDate.minusMonths(1).lengthOfMonth());

        List<MutualFundHoldingEntity> curr = holdingRepository.findByAtDate(asOfDate);
        List<MutualFundHoldingEntity> prev = holdingRepository.findByAtDate(prevDate);

        Map<Long, MutualFundHoldingEntity> prevByStockId = prev.stream()
                .collect(Collectors.toMap(MutualFundHoldingEntity::getStockId, h -> h, (a, b) -> a));

        List<StockInsight> all = curr.stream()
                .map(c -> toInsight(c, prevByStockId.get(c.getStockId())))
                .filter(Objects::nonNull)
                .toList();

        List<StockInsight> existing = all.stream()
                .filter(s -> s.getValueChangePct() != null)
                .toList();

        List<StockInsight> newlyAdded = all.stream()
                .filter(s -> s.getValueChangePct() == null)
                .peek(s -> s.setType("new"))
                .toList();

        List<StockInsight> growth = existing.stream()
                .sorted(Comparator.comparing(StockInsight::getValueChangePct).reversed())
                .peek(s -> s.setType("growth"))
                .toList();

        List<StockInsight> avoid = existing.stream()
                .sorted(Comparator.comparing(StockInsight::getValueChangePct))
                .peek(s -> s.setType("avoid"))
                .toList();

        List<StockInsight> stable = existing.stream()
                .filter(s -> Math.abs(s.getWeightChangePct()) <= STABLE_WEIGHT_DELTA_PCT)
                .sorted(Comparator.comparing(StockInsight::getNetAssetPct).reversed())
                .peek(s -> s.setType("stable"))
                .toList();

        return InvestmentInsightsResponse.builder()
                .currDate(asOfDate)
                .prevDate(prevDate)
                .stable(limitPerCategory(stable))
                .growth(limitPerCategory(growth))
                .avoid(limitPerCategory(avoid))
                .newlyAdded(limitPerCategory(newlyAdded))
                .build();
    }

    private Map<String, List<StockInsight>> limitPerCategory(List<StockInsight> list) {
        return list.stream()
                .collect(Collectors.groupingBy(
                        s -> defaultStr(s.getMarketCapCategory(), "Unknown"),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(),
                                l -> l.stream().limit(5).toList())
                ));
    }

    private StockInsight toInsight(MutualFundHoldingEntity curr, MutualFundHoldingEntity prev) {
        StockInfoEntity s = curr.getStockInfoEntity();
        if (s == null) return null;

        double currVal = safeD(curr.getMarketValue());
        double prevVal = prev != null ? safeD(prev.getMarketValue()) : 0.0;

        Double valueChange = currVal - prevVal;
        Double valueChangePct = (prev != null && prevVal > 0.0)
                ? ((currVal - prevVal) / prevVal) * 100.0
                : null;

        double currW = safeD(curr.getNetAssetPct());
        double prevW = (prev != null) ? safeD(prev.getNetAssetPct()) : 0.0;
        Double weightChangePct = currW - prevW;

        return StockInsight.builder()
                .symbol(s.getSymbol())
                .company(s.getCompany())
                .marketCapCategory(s.getMarketCapCategory())
                .quantity(safeL(curr.getQuantity()))
                .netAssetPct(currW)
                .marketCap(s.getMarketCap())
                .valueChange(valueChange)
                .valueChangePct(valueChangePct)
                .weightChangePct(weightChangePct)
                .type(valueChangePct == null ? "new" : "unknown")
                .build();
    }

    private static double safeD(Double v) { return v == null ? 0.0 : v; }
    private static long safeL(Long v) { return v == null ? 0L : v; }
    private static String defaultStr(String v, String d) { return v == null ? d : v; }
}
