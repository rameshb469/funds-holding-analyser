package com.rms.funds.holdings.analyser.test;

import com.rms.funds.holdings.analyser.dto.McpRecordDto;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.McpCsvLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static com.rms.funds.holdings.analyser.utility.AppConst._100K;
import static java.util.stream.Collectors.toMap;

//@Component
@RequiredArgsConstructor
public class MarketCapUpdateService implements CommandLineRunner {

    private final StockInfoRepository stockInfoRepository;
    private final McpCsvLoader mcpCsvLoader;

    @Override
    public void run(String... args) throws Exception {
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
                notUpdatedStocks.add(symbol);
            }

        }

        stockInfoRepository.saveAll(updatedValues);
        System.out.println("Total Stocks updated with market cap data: " + updatedValues.size() );

        System.out.println("Stocks not updated due to missing market cap data: " + notUpdatedStocks);

        System.out.println("MarketCapUpdateService is done...");
    }
}
