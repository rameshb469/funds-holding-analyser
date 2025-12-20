package com.rms.funds.holdings.analyser.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.service.StockDataFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rms.funds.holdings.analyser.utility.AppConst._100K;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataFetchServiceImpl implements StockDataFetchService {

    private final StockInfoRepository stockRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Long default_sector_id = 38L;
    private static final Long default_industry_id = 180L;

    private static final String PYTHON_PATH = "python3";  // change if using Windows

    @Override
    public Optional<StockInfoEntity> findByIsinNumber(String isinCsv) {
        Optional<StockInfoEntity> dbValues = Optional.empty();

        try {
            // 1️⃣ Check DB first
            dbValues = stockRepository.findByIsinNumber(isinCsv);
            if (dbValues.isPresent()) {
                log.info("✅ Found existing record for ISIN {}", isinCsv);
                return dbValues;
            }

            // 2️⃣ Load Python script from resources
            InputStream scriptStream = getClass().getResourceAsStream("/scripts/fetch_by_isin.py");
            if (scriptStream == null) {
                log.error("❌ Python script not found in resources/scripts/");
                return Optional.empty();
            }

            // 3️⃣ Copy script to a temp file
            Path tempScript = Files.createTempFile("fetch_by_isin_", ".py");
            Files.copy(scriptStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            scriptStream.close();

            log.info("🚀 Running Python script: {}", tempScript.toAbsolutePath());

            // 4️⃣ Build process (keep stderr separate)
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_PATH,
                    tempScript.toAbsolutePath().toString(),
                    isinCsv
            );

            Process process = pb.start();

            // 5️⃣ Capture stdout (JSON)
            StringBuilder jsonOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonOutput.append(line);
                }
            }

            // 6️⃣ Capture stderr (logs)
            new Thread(() -> {
                try (BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        log.info("🐍 {}", errLine);
                    }
                } catch (IOException e) {
                    log.warn("Error reading Python stderr: {}", e.getMessage());
                }
            }).start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("❌ Python script exited with code {}", exitCode);
                return Optional.empty();
            }

            String cleanJson = jsonOutput.toString().trim();
            if (cleanJson.isEmpty() || !cleanJson.startsWith("[")) {
                log.error("❌ Invalid JSON output: {}", cleanJson);
                return Optional.empty();
            }

            // 7️⃣ Parse JSON output
            List<Map<String, Object>> stockList = objectMapper.readValue(
                    cleanJson,
                    new TypeReference<>() {}
            );

            for (Map<String, Object> s : stockList) {
                StockInfoEntity stock = StockInfoEntity.builder()
                        .symbol((String) s.get("symbol"))
                        .company((String) s.get("company"))
                      //  .sector(default_sector_id) // default sector
                        .series((String) s.get("series"))
                        .paidUpValue(parseInt(s.get("paidUpValue")))
                        .marketLot(parseInt(s.get("marketLot")))
                        .faceValue(parseInt(s.get("faceValue")))
                        .isinNumber((String) s.get("isinNumber"))
                        .marketCap(parseLong(s.get("marketCap"))/_100K)
                        .totalFloatingShares(parseLong(s.get("floatShares"))) // ✅ match Python key
                        .sharesOutstanding(parseLong(s.get("sharesOutstanding")))
                        .rank(parseInt(s.get("rank")))
                        .marketCapCategory((String) s.get("marketCapCategory"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                Optional<StockInfoEntity> existing = stockRepository.findBySymbol(stock.getSymbol());
                if (existing.isPresent()) {
                    log.info("⚠️ Stock with symbol {} already exists. Skipping insert.", stock.getSymbol());
                    dbValues = existing;
                } else {
                    log.info("⚠️ Stock with symbol {} doesn't exists. inserting it.", stock.getSymbol());
                    dbValues = Optional.of(stockRepository.save(stock));
                    log.info("✅ Inserted stock {}", stock.getSymbol());
                }
            }

            Files.deleteIfExists(tempScript);
            log.info("✅ Completed fetch for ISIN {}", isinCsv);

            return dbValues;

        } catch (Exception e) {
            log.error("❌ Error executing Python script", e);
            return Optional.empty();
        }
    }

    private Long parseLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
