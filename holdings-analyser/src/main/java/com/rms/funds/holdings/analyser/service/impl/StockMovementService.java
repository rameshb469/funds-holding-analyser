package com.rms.funds.holdings.analyser.service.impl;

import com.opencsv.CSVReader;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockPriceHistoryRepository historyRepository;
    private final StockInfoRepository stockInfoRepository;

    /**
     * Compute stock movements for the given tradeDate (defaults to yesterday). Optionally compare to a Bhav Copy CSV provided
     * as a local path or a URL (zip or csv). If csvSource is null, no CSV comparison is done.
     */
    public List<StockMovementResult> computeMovements(LocalDate tradeDate, String csvSource) throws Exception {
        LocalDate date = (tradeDate != null) ? tradeDate : LocalDate.now().minusDays(1);

        // load CSV map if provided: map by ISIN and by symbol to close price
        Map<String, BigDecimal> csvByIsin = new HashMap<>();
        Map<String, BigDecimal> csvBySymbol = new HashMap<>();
        if (csvSource != null && !csvSource.trim().isEmpty()) {
            try (Reader reader = openCsvReader(csvSource)) {
                if (reader != null) {
                    try (CSVReader csvReader = new CSVReader(reader)) {
                        String[] header = csvReader.readNext();
                        String[] line;
                        while ((line = csvReader.readNext()) != null) {
                            if (line.length < 18) continue; // ensure enough columns
                            String sctySrs = safe(line,8);
                            if (!"EQ".equalsIgnoreCase(sctySrs)) continue;
                            String isin = safe(line,6);
                            String symbol = safe(line,7);
                            String closeStr = safe(line,17);
                            try {
                                BigDecimal close = new BigDecimal(closeStr.trim());
                                if (isin != null && !isin.isEmpty()) csvByIsin.put(isin, close);
                                if (symbol != null && !symbol.isEmpty()) csvBySymbol.put(symbol, close);
                            } catch (Exception e) {
                                // ignore parse errors
                            }
                        }
                    }
                }
            }
        }

        List<StockMovementResult> results = new ArrayList<>();

        // fetch all history rows for the trade date
        List<StockPriceHistory> todays = historyRepository.findByTradeDate(date);
        if (todays == null) todays = Collections.emptyList();

        for (StockPriceHistory cur : todays) {
            try {
                if (cur.getSecuritySeries() == null || !"EQ".equalsIgnoreCase(cur.getSecuritySeries())) continue;

                StockMovementResult r = new StockMovementResult();
                r.setTradeDate(date);
                r.setSymbol(cur.getTickerSymbol());
                r.setIsin(cur.getIsin());
                if (cur.getStock() != null) r.setStockId(cur.getStock().getId());
                r.setClose(cur.getClosePrice());

                // find previous
                Long stockId = (cur.getStock() != null) ? cur.getStock().getId() : null;
                if (stockId == null) {
                    // attempt to lookup via ISIN or symbol
                    if (cur.getIsin() != null) {
                        Optional<StockInfoEntity> byIsin = stockInfoRepository.findByIsinNumber(cur.getIsin());
                        if (byIsin.isPresent()) stockId = byIsin.get().getId();
                    }
                    if (stockId == null && cur.getTickerSymbol() != null) {
                        Optional<StockInfoEntity> bySym = stockInfoRepository.findBySymbol(cur.getTickerSymbol());
                        if (bySym.isPresent()) stockId = bySym.get().getId();
                    }
                }

                if (stockId != null) {
                    StockPriceHistory prev = historyRepository.findTopByStockIdAndTradeDateBeforeOrderByTradeDateDesc(stockId, date);
                    if (prev != null && prev.getClosePrice() != null) {
                        r.setPreviousDate(prev.getTradeDate());
                        r.setPreviousClose(prev.getClosePrice());
                        BigDecimal abs = r.getClose() != null ? r.getClose().subtract(r.getPreviousClose()) : null;
                        r.setAbsoluteChange(abs);
                        if (abs != null && r.getPreviousClose() != null && r.getPreviousClose().compareTo(BigDecimal.ZERO) != 0) {
                            double pct = abs.divide(r.getPreviousClose(), 6, BigDecimal.ROUND_HALF_UP).doubleValue() * 100.0;
                            r.setPercentChange(pct);
                        }
                    } else {
                        r.setNote("No previous record found");
                    }
                } else {
                    r.setNote("Stock master not found for this record");
                }

                // CSV comparison if available
                if (!csvByIsin.isEmpty() || !csvBySymbol.isEmpty()) {
                    BigDecimal csvClose = null;
                    if (r.getIsin() != null && csvByIsin.containsKey(r.getIsin())) csvClose = csvByIsin.get(r.getIsin());
                    if (csvClose == null && r.getSymbol() != null && csvBySymbol.containsKey(r.getSymbol())) csvClose = csvBySymbol.get(r.getSymbol());
                    if (csvClose != null) {
                        r.setCsvClose(csvClose);
                        boolean matches = (r.getClose() != null && r.getClose().compareTo(csvClose) == 0);
                        r.setCsvMatches(matches);
                        if (!matches) r.setNote((r.getNote() != null ? r.getNote() + "; " : "") + "CSV close differs");
                    } else {
                        // no csv record found for this stock
                        r.setNote((r.getNote() != null ? r.getNote() + "; " : "") + "No CSV record");
                    }
                }

                results.add(r);
            } catch (Exception ex) {
                // collect error note
                StockMovementResult rErr = new StockMovementResult();
                rErr.setNote("Error processing row: " + ex.getMessage());
                results.add(rErr);
            }
        }

        return results;
    }

    private Reader openCsvReader(String source) throws Exception {
        if (source == null) return null;
        source = source.trim();
        if (source.startsWith("http://") || source.startsWith("https://")) {
            // download
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> resp = rest.exchange(URI.create(source), HttpMethod.GET, entity, byte[].class);
            if (!resp.getStatusCode().is2xxSuccessful()) return null;
            byte[] body = resp.getBody();
            if (source.endsWith(".zip")) {
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(body));
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) return null;
                return new InputStreamReader(zis);
            } else {
                return new InputStreamReader(new ByteArrayInputStream(body));
            }
        } else {
            // assume local file path
            File f = new File(source);
            if (!f.exists()) return null;
            if (source.endsWith(".zip")) {
                ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) return null;
                return new InputStreamReader(zis);
            } else {
                return new FileReader(f);
            }
        }
    }

    private String safe(String[] arr, int idx) {
        if (arr == null || idx >= arr.length) return null;
        return arr[idx] != null ? arr[idx].trim() : null;
    }
}

