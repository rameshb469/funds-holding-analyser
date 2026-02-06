package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.dto.McpRecordDto;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.model.MarketCapCategoryType;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static com.rms.funds.holdings.analyser.utility.AppConst._100K;
import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketCapUpdateService {

    private final StockInfoRepository stockInfoRepository;
    private final McpCsvLoader mcpCsvLoader;

    private static final Set<String> INVALID_SYMBOLS = Set.of("WORTH",
            "FSC", "MCDOWELL-N", "JPASSOCIAT", "CASH", "JETAIRWAYS", "544021", "530305",
            "TV18BRDCST", "TECHIN", "GAYAPROJ", "BINANIIND", "JUBLINDS", "IBULHSGFIN", "SPICEJET");

    public void updateMarketCap() throws Exception {
        // Implement market cap update logic here

        Map<String, StockInfoEntity> stockInfoEntityMap = stockInfoRepository.findAll().stream()
                .collect(toMap(
                        StockInfoEntity::getSymbol,
                        Function.identity()
                ));
        Map<String, McpRecordDto> marketCapMap = mcpCsvLoader.loadAll().stream()
                .filter(mcp -> mcp.getSymbol() != null && mcp.getMarketCapRs() != null)
                .collect(toMap(
                        mcp -> mcp.getSymbol().toUpperCase().trim(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<StockInfoEntity> updatedValues = new ArrayList<>();
        Set<String> notUpdatedStocks = new LinkedHashSet<>();
        for (String symbol : stockInfoEntityMap.keySet()) {
            if (marketCapMap.containsKey(symbol)) {
                StockInfoEntity stockInfo = stockInfoEntityMap.get(symbol);
                McpRecordDto mcpRecord = marketCapMap.get(symbol);
                updatedValues.add(
                        stockInfo.toBuilder()
                                .marketCap(
                                        mcpRecord.getMarketCapRs() != null ?
                                                mcpRecord.getMarketCapRs().longValue()/(_100K) : null
                                )
                                .faceValue(
                                        mcpRecord.getFaceValueRs() != null ?
                                                mcpRecord.getFaceValueRs().intValue() : null
                                )
                                .updatedAt(LocalDateTime.now())
                                .build()
                );
            } else {
                if (!INVALID_SYMBOLS.contains(symbol)) {
                    log.info("Market cap data not found for symbol: {}", symbol);
                    notUpdatedStocks.add(symbol);
                } else {
                    log.info("Skipping invalid symbol: {}", symbol);
                }
            }

        }

        stockInfoRepository.saveAll(updatedValues);
        log.info("Total Stocks updated with market cap data: {}", updatedValues.size());

        log.info("Stocks not updated due to missing market cap data: {}", notUpdatedStocks);

        log.info("MarketCapUpdateService is done...");

        updateRanks();
    }


    void updateRanks() {
        List<StockInfoEntity> allStocks = stockInfoRepository.findAllByOrderByMarketCapDesc();
        int rank = 1;
        List<StockInfoEntity> updatedStocks = new ArrayList<>();
        for (StockInfoEntity stock : allStocks) {
            updatedStocks.add(
                    stock.toBuilder()
                            .marketCapCategory(getMarketCapCategory(rank).name())
                            .rank(rank++)
                            .build()
            );
        }
        stockInfoRepository.saveAll(updatedStocks);
        log.info("Updated market cap ranks for {} stocks", updatedStocks.size());
    }


    private MarketCapCategoryType getMarketCapCategory(int rank) {
        if (rank <= 100) {
            return MarketCapCategoryType.LARGE_CAP;
        } else if (rank <= 250) {
            return MarketCapCategoryType.MID_CAP;
        } else if (rank <= 550) {
            return MarketCapCategoryType.SMALL_CAP;
        } else {
            return MarketCapCategoryType.MIRCO_CAP;
        }
    }
}
