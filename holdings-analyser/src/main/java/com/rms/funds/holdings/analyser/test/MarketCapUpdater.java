package com.rms.funds.holdings.analyser.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
@RequiredArgsConstructor
public class MarketCapUpdater implements CommandLineRunner {

    private final StockInfoRepository stockInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PYTHON_SCRIPT_PATH = "scripts/fetch_stock_details.py";

    @Transactional
    public void updateMissingMarketCap() {
        List<StockInfoEntity> stocks = stockInfoRepository.findByMarketCapCategoryIsNull();

        if (stocks.isEmpty()) {
            System.out.println("No stocks with missing marketCap found.");
            return;
        }

        List<String> symbols = stocks.stream()
                .map(StockInfoEntity::getSymbol)
                .collect(Collectors.toList());

        try {
            // Locate Python script in resources
            ClassLoader classLoader = getClass().getClassLoader();
            File pythonFile = new File(classLoader.getResource(PYTHON_SCRIPT_PATH).getFile());

            // Build command (use python3 explicitly)
            String cmd = "python3 " + pythonFile.getAbsolutePath() + " '"
                    + objectMapper.writeValueAsString(symbols) + "'";

            Process process = Runtime.getRuntime().exec(cmd);

// Capture stdout
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = stdout.readLine()) != null) {
                output.append(line);
            }
            stdout.close();

// Capture stderr for debugging logs
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stderr.readLine()) != null) {
                System.err.println(line);
            }
            stderr.close();

// Parse JSON safely
            if (output.length() > 0) {
                List<Map<String,Object>> enrichedStocks = objectMapper.readValue(
                        output.toString(),
                        new TypeReference<List<Map<String,Object>>>() {}
                );
                // ... update DB
                // Update database
                for (Map<String, Object> data : enrichedStocks) {
                    String symbol = (String) data.get("symbol");
                    StockInfoEntity stock = stocks.stream()
                            .filter(s -> s.getSymbol().equals(symbol))
                            .findFirst()
                            .orElse(null);

                    if (stock != null) {
                        stock.setMarketCap(data.get("marketCap") != null ? ((Number)data.get("marketCap")).longValue() : null);
                        stock.setSharesOutstanding(data.get("sharesOutstanding") != null ? ((Number)data.get("sharesOutstanding")).longValue() : null);
                        stock.setTotalFloatingShares(data.get("floatShares") != null ? ((Number)data.get("floatShares")).longValue() : null);
                        stock.setMarketCapCategory((String) data.get("marketCapCategory"));
                    }
                }

                stockInfoRepository.saveAll(stocks);
                System.out.println("✅ Updated " + enrichedStocks.size() + " stocks with marketCap & category.");

            } else {
                System.err.println("❌ Python did not return any JSON.");
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to update marketCap: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        updateMissingMarketCap();
    }
}
