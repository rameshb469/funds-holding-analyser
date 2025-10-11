package com.rms.funds.hodings.analyser.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.service.StockDataFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataFetchServiceImpl implements StockDataFetchService {

    private final StockInfoRepository stockRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PYTHON_PATH = "python3";  // or "python" for Windows

    @Override
    public Optional<StockInfoEntity> findByIsinNumber(String isinCsv) {

        try {

            Optional<StockInfoEntity> dbValues = stockRepository.findByIsinNumber(isinCsv);
            if (dbValues.isPresent()) {
                return dbValues;
            }

            // 1️⃣ Load the script from classpath
            InputStream scriptStream = getClass().getResourceAsStream("/scripts/fetch_by_isin.py");
            if (scriptStream == null) {
                log.error("❌ Script not found in resources/scripts/");
                Optional.empty();
            }

            // 2️⃣ Copy to temporary file
            Path tempScript = Files.createTempFile("fetch_by_isin_", ".py");
            Files.copy(scriptStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            scriptStream.close();

            log.info("🚀 Running Python script from: {}", tempScript.toAbsolutePath());

            // 3️⃣ Run Python process
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_PATH,
                    tempScript.toAbsolutePath().toString(),
                    isinCsv
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 4️⃣ Capture output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("❌ Python script exited with code {}", exitCode);
                log.error("Output:\n{}", output);
                return Optional.empty();
            }

            // 5️⃣ Parse JSON output and insert
            List<Map<String, Object>> stockList = objectMapper.readValue(
                    output.toString(),
                    new TypeReference<>() {}
            );

            for (Map<String, Object> s : stockList) {
                StockInfoEntity stock = StockInfoEntity.builder()
                        .symbol((String) s.get("symbol"))
                        .company((String) s.get("company"))
                        .series((String) s.get("series"))
                        .paidUpValue(parseInt(s.get("paidUpValue")))
                        .marketLot(parseInt(s.get("marketLot")))
                        .faceValue(parseInt(s.get("faceValue")))
                        .isinNumber((String) s.get("isinNumber"))
                        .marketCap(parseLong(s.get("marketCap")))
                        .totalFloatingShares(parseLong(s.get("totalFloatingShares")))
                        .sharesOutstanding(parseLong(s.get("sharesOutstanding")))
                        .rank(parseInt(s.get("rank")))
                        .marketCapCategory((String) s.get("marketCapCategory"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                dbValues = Optional.of(stockRepository.save(stock));
            }

            log.info("✅ Inserted {} stocks into DB", stockList.size());

            // 6️⃣ Clean up
            Files.deleteIfExists(tempScript);
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

