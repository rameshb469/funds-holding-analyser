package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.hodings.analyser.entity.IndustryEntity;
import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import com.rms.funds.hodings.analyser.entity.SectorEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.model.IdName;
import com.rms.funds.hodings.analyser.repository.MfHoldingRepository;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.service.StockHoldingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.rms.funds.hodings.analyser.utility.AppConst._100K;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class StockHoldingServiceImpl implements StockHoldingService {

    private final MfHoldingRepository mfHoldingRepository;
    private final StockInfoRepository stockInfoRepository;

    @Override
    public StockHoldingDto getMetrics(Long stockId) {

        StockInfoEntity stockInfoEntity = stockInfoRepository.findById(stockId)
                .orElseThrow(() -> new NoSuchElementException("Invalid stockId : "+stockId));

        Map<LocalDate, List<MfHoldingEntity>> groupByDates = mfHoldingRepository.findByStockId(stockId).stream()
                .collect(groupingBy(
                        MfHoldingEntity::getAtDate,
                        () -> new TreeMap<>(Comparator.reverseOrder()), // ensures descending order of keys
                        Collectors.toList()
                ));

        // find first entry (latest date group)
        List<Map.Entry<LocalDate, List<MfHoldingEntity>>> top12 = groupByDates.entrySet().stream()
                                                                                    .limit(12)
                                                                                    .toList();
        StockHoldingDto.StockHoldingDtoBuilder stockHoldingDtoBuilder = StockHoldingDto.builder();
        if (!CollectionUtils.isEmpty(top12)) {
            int firstIndex = 0;
            long currQuantity = top12.get(firstIndex).getValue().stream().mapToLong(MfHoldingEntity::getQuantity).sum();
            double currMarketValue = top12.get(firstIndex).getValue().stream().mapToDouble(MfHoldingEntity::getMarketValue).sum();
            int currMfCount = top12.get(firstIndex).getValue().size();
            double currAvgPrice = top12.get(firstIndex).getValue().stream().mapToDouble(e -> e.getAvgPrice()*_100K).average().orElse(0.0);

            stockHoldingDtoBuilder.totalQuantity(currQuantity);
            stockHoldingDtoBuilder.valuation(currMarketValue);
            stockHoldingDtoBuilder.totalMfCount(currMfCount);
            stockHoldingDtoBuilder.avgPrice(currAvgPrice);


            List<Pair<LocalDate,List<StockHoldingDto.MFQuantity>>> quantityChangeByDates = new ArrayList<>();
            var quantityByMfName = top12.get(firstIndex).getValue().stream()
                    .map(e -> StockHoldingDto.MFQuantity.builder()
                            .mfId(e.getMfId())
                            .mfName(e.getMfEntity().getName())
                            .quantity(e.getQuantity())
                            .build())
                    .toList();
            quantityChangeByDates.add(Pair.of(top12.get(firstIndex).getKey(), quantityByMfName));

            stockHoldingDtoBuilder.mfDistribution(Map.of(top12.get(firstIndex).getKey(), top12.get(firstIndex).getValue().stream()
                    .map(e -> StockHoldingDto.MFQuantity.builder()
                            .mfId(e.getMfId())
                            .mfName(e.getMfEntity().getName())
                            .quantity(e.getQuantity())
                            .build()).toList()));

            stockHoldingDtoBuilder.valuationChangeByDates(top12.get(firstIndex).getValue().stream()
                    .map(e -> Pair.of(e.getAtDate(), e.getMarketValue()))
                    .toList());

            int secondIndex = 1;
            if (top12.size() >= 2) {
                long prevQuantity = top12.get(secondIndex).getValue().stream().mapToLong(MfHoldingEntity::getQuantity).sum();
                long diff =  currQuantity - prevQuantity;
                stockHoldingDtoBuilder.quantityChange(prevQuantity == 0 ? 0.0 : (diff * 100.0 / prevQuantity));

                double prevMarketValue = top12.get(secondIndex).getValue().stream().mapToDouble(MfHoldingEntity::getMarketValue).sum();
                double diffMarketValue =  currMarketValue - prevMarketValue;
                stockHoldingDtoBuilder.valuationChange(prevMarketValue == 0.0 ? 0.0 : (diffMarketValue * 100.0 / prevMarketValue));

                int prevMfCount = top12.get(secondIndex).getValue().size();
                int diffMfCount = currMfCount - prevMfCount;
                stockHoldingDtoBuilder.totalMfCountChange(diffMfCount);

                double prevAvgPrice = top12.get(secondIndex).getValue().stream().mapToDouble(e -> e.getAvgPrice()*_100K).average().orElse(0.0);
                double diffAvgPrice =  currAvgPrice - prevAvgPrice;
                stockHoldingDtoBuilder.avgPriceChange(diffAvgPrice);
                quantityChangeByDates.addAll(top12.stream().skip(1)
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue().stream()
                                .map(e -> StockHoldingDto.MFQuantity.builder()
                                        .mfId(e.getMfId())
                                        .mfName(e.getMfEntity().getName())
                                        .quantity(e.getQuantity())
                                        .build())
                                .toList())).toList());
            }

            stockHoldingDtoBuilder.quantityChangeByDates(quantityChangeByDates);
        }

        SectorEntity sectorEntity = stockInfoEntity.getSector();
        IndustryEntity industryEntity = stockInfoEntity.getIndustry();

        stockHoldingDtoBuilder.stockId(stockId);
        stockHoldingDtoBuilder.stockName(stockInfoEntity.getCompany());
        stockHoldingDtoBuilder.symbol(stockInfoEntity.getSymbol());
        stockHoldingDtoBuilder.sector(IdName.builder().id(sectorEntity.getId()).name(sectorEntity.getName()).build());
        stockHoldingDtoBuilder.industry(IdName.builder().id(industryEntity.getId()).name(industryEntity.getName()).build());

        return stockHoldingDtoBuilder.build();
    }

    @Override
    public HoldingChangeMetricDto getHoldingChangeMetrics(LocalDate currentDate) {
        if (currentDate == null) {
            currentDate = mfHoldingRepository.findAll().stream().max(Comparator.comparing(MfHoldingEntity::getAtDate))
                    .map(MfHoldingEntity::getAtDate)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to fetch last date reports"));
        }
        LocalDate prevDate = currentDate.minusMonths(1)
                .withDayOfMonth(currentDate.minusMonths(1).lengthOfMonth());

        List<Object[]> rows = mfHoldingRepository.findHoldingsForDates(currentDate, prevDate);

        Map<Long, Double> mfAumCache = new HashMap<>();
        Map<LocalDate, Map<Long, ExposureData>> exposureByDate = new HashMap<>();

        // ---- BUILD EXPOSURES ----
        for (Object[] row : rows) {
            Long stockId = (Long) row[0];
            Long mfId = (Long) row[1];
            LocalDate date = (LocalDate) row[2];
            double valuation = ((Double) row[3])*_100K;
            Double netAssetPct = (Double) row[4];

            if (netAssetPct == null || netAssetPct == 0) continue;

            mfAumCache.computeIfAbsent(mfId, id -> valuation / (netAssetPct / 100.0));
            double aum = mfAumCache.get(mfId);
            double exposure = aum * (netAssetPct / 100.0);

            exposureByDate
                    .computeIfAbsent(date, d -> new HashMap<>())
                    .computeIfAbsent(stockId, s -> new ExposureData())
                    .add(exposure, netAssetPct, mfId);
        }

        Map<Long, ExposureData> currData = exposureByDate.getOrDefault(currentDate, Map.of());
        Map<Long, ExposureData> prevData = exposureByDate.getOrDefault(prevDate, Map.of());

        List<HoldingChangeMetricDto.StockPerformanceChange> changes = new ArrayList<>();

        // ---- BUILD DTOs ----
        for (Long stockId : currData.keySet()) {
            ExposureData curr = currData.getOrDefault(stockId, new ExposureData());
            ExposureData prev = prevData.getOrDefault(stockId, new ExposureData());

            double exposureChange = curr.totalExposure - prev.totalExposure;
            double avgCurrPct = curr.totalPct / Math.max(curr.fundCount, 1);
            double avgPrevPct = prev.totalPct / Math.max(prev.fundCount, 1);
            double relativeChangePct = avgPrevPct == 0 ? 0 :
                    ((avgCurrPct - avgPrevPct) / avgPrevPct) * 100.0;
            int fundCountChange = curr.fundIds.size() - prev.fundIds.size();

            StockInfoEntity stock = stockInfoRepository.findById(stockId).orElse(null);
            if (stock == null) continue;
            changes.add(HoldingChangeMetricDto.StockPerformanceChange.builder()
                            .id(stockId)
                            .name(stock.getCompany())
                            .symbol(stock.getSymbol())
                            .sector(IdName.builder()
                                    .id(stock.getSector().getId()).name(stock.getSector().getName())
                                    .build())
                            .industry(IdName.builder()
                                    .id(stock.getIndustry().getId()).name(stock.getIndustry().getName())
                                    .build())
                            .exposureChange(exposureChange)
                            .relativeChangePct(relativeChangePct)
                            .fundCountChange(fundCountChange)
                    .build());
        }

// ---- NORMALIZATION FOR COMPOSITE SCORE ----
        double maxExposure = changes.stream().mapToDouble(HoldingChangeMetricDto.StockPerformanceChange::getExposureChange)
                .map(Math::abs).max().orElse(1);
        double maxRelative = changes.stream().mapToDouble(c -> Math.abs(c.getRelativeChangePct())).max().orElse(1);
        int maxFundCount = changes.stream().mapToInt(c -> Math.abs(c.getFundCountChange())).max().orElse(1);

        double w1 = 0.5, w2 = 0.3, w3 = 0.2;

        for (HoldingChangeMetricDto.StockPerformanceChange dto : changes) {
            double normExposure = dto.getExposureChange() / maxExposure;
            double normRelative = dto.getRelativeChangePct() / maxRelative;
            double normFund = (double) dto.getFundCountChange() / maxFundCount;

            double score = w1 * normExposure + w2 * normRelative + w3 * normFund;
            dto.setScore(score);
        }

// ---- BUILD RANKED LISTS ----
        List<HoldingChangeMetricDto.StockPerformanceChange> topByExposure = changes.stream()
                .sorted((a, b) -> Double.compare(b.getExposureChange(), a.getExposureChange()))
                .limit(10).toList();

        List<HoldingChangeMetricDto.StockPerformanceChange> topByRelativePct = changes.stream()
                .sorted((a, b) -> Double.compare(b.getRelativeChangePct(), a.getRelativeChangePct()))
                .limit(10).toList();

        List<HoldingChangeMetricDto.StockPerformanceChange> topByFundCount = changes.stream()
                .sorted((a, b) -> Integer.compare(b.getFundCountChange(), a.getFundCountChange()))
                .limit(10).toList();

        List<HoldingChangeMetricDto.StockPerformanceChange> topOverall = changes.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(10).toList();

// ---- RETURN ALL ----
        Map<String, List<HoldingChangeMetricDto.StockPerformanceChange>> stockPerformance = new HashMap<>();
        stockPerformance.put("top10ByExposure", topByExposure);
        stockPerformance.put("top10ByRelativePct", topByRelativePct);
        stockPerformance.put("top10ByFundCount", topByFundCount);
        stockPerformance.put("top10Overall", topOverall);

        return HoldingChangeMetricDto.builder()
                .stockPerformance(stockPerformance)
                .build();
    }

    @Data
    private static class ExposureData {
        double totalExposure = 0.0;
        double totalPct = 0.0;
        int fundCount = 0;
        Set<Long> fundIds = new HashSet<>();

        void add(double exposure, double pct, Long mfId) {
            this.totalExposure += exposure;
            this.totalPct += pct;
            this.fundCount++;
            this.fundIds.add(mfId);
        }
    }
}
