package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.dto.McpRecordDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class McpCsvLoader {

    /**
     * Load the default file `mcp19122025.csv` from the classpath (resources).
     */
    public List<McpRecordDto> loadAll() throws IOException {
        return loadFromResource("mcap19122025.csv");
    }

    public List<McpRecordDto> loadFromResource(String resourceName) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            return Collections.emptyList();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) return Collections.emptyList();
            String[] header = splitCsvLine(headerLine);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                String normalized = normalizeHeader(header[i]);
                idx.put(normalized, i);
            }

            // normalized keys we want to extract
            String SYMBOL_KEY = normalizeHeader("Symbol");
            String SERIES_KEY = normalizeHeader("Series");
            String MARKET_CAP_KEY = normalizeHeader("Market Cap(Rs.)");
            String FACE_VALUE_KEY = normalizeHeader("Face Value(Rs.)");

            List<McpRecordDto> result = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = splitCsvLine(line);

                String symbol = getValue(cols, idx, SYMBOL_KEY);
                String series = getValue(cols, idx, SERIES_KEY);
                BigDecimal marketCap = getBigDecimal(cols, idx, MARKET_CAP_KEY);
                BigDecimal faceValue = getBigDecimal(cols, idx, FACE_VALUE_KEY);

                McpRecordDto dto = McpRecordDto.builder()
                        .symbol(symbol)
                        .series(series)
                        .marketCapRs(marketCap)
                        .faceValueRs(faceValue)
                        .build();
                result.add(dto);
            }
            return result;
        }
    }

    private static String getValue(String[] cols, Map<String, Integer> idx, String normalizedKey) {
        Integer pos = idx.get(normalizedKey);
        if (pos == null || pos >= cols.length) return null;
        return cols[pos].trim();
    }

    private static BigDecimal getBigDecimal(String[] cols, Map<String, Integer> idx, String normalizedKey) {
        Integer pos = idx.get(normalizedKey);
        if (pos == null || pos >= cols.length) return null;
        String raw = cols[pos].trim();
        if (raw.isEmpty()) return null;
        return parseBigDecimal(raw);
    }

    private static String normalizeHeader(String header) {
        if (header == null) return "";
        return header.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static BigDecimal parseBigDecimal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("[,\"]", "").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // simple CSV splitter that handles quoted fields with commas
    private static String[] splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!inQuotes && (c == '"' || c == '\'')) {
                inQuotes = true;
                quoteChar = c;
                continue;
            }
            if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    parts.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }
}
