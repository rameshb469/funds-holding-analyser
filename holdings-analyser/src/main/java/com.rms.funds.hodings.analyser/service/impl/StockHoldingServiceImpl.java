package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.hodings.analyser.entity.IndustryEntity;
import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import com.rms.funds.hodings.analyser.entity.SectorEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.model.IdName;
import com.rms.funds.hodings.analyser.repository.MfHoldingRepository;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.service.StockHoldingService;
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

        SectorEntity sectorEntity = stockInfoEntity.getSectorEntity();
        IndustryEntity industryEntity = stockInfoEntity.getIndustry();

        stockHoldingDtoBuilder.stockId(stockId);
        stockHoldingDtoBuilder.stockName(stockInfoEntity.getCompany());
        stockHoldingDtoBuilder.symbol(stockInfoEntity.getSymbol());
        stockHoldingDtoBuilder.sector(IdName.builder().id(sectorEntity.getId()).name(sectorEntity.getName()).build());
        stockHoldingDtoBuilder.industry(IdName.builder().id(industryEntity.getId()).name(industryEntity.getName()).build());

        return stockHoldingDtoBuilder.build();
    }
}
