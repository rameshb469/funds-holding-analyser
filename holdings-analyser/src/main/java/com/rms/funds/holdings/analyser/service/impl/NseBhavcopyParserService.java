package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import com.rms.funds.holdings.analyser.service.BhavcopyParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;


// BhavcopyParserService.java

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.opencsv.CSVReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class NseBhavcopyParserService implements BhavcopyParserService {

    private final StockPriceHistoryRepository repository;
    private final StockInfoRepository stockInfoRepository;

    @Override
    public void parseAndSave(File csvFile) throws Exception {
        try (Reader reader = Files.newBufferedReader(csvFile.toPath());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext(); // skip header
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                // Only insert EQ segments
               // if (!"EQ".equalsIgnoreCase(line[8].trim())) continue;
                StockPriceHistory entity = new StockPriceHistory();
                entity.setTradeDate(parseDate(line[0])); // TradDt
                entity.setBizDate(parseDate(line[1])); // BizDt
                entity.setSegment(line[2]); // Sgmt
                entity.setSource(line[3]); // Src
                entity.setFinancialInstrumentType(line[4]); // FinInstrmTp
                entity.setFinancialInstrumentId(line[5]); // FinInstrmId
                entity.setIsin(line[6]); // ISIN
                entity.setTickerSymbol(line[7]); // TckrSymb
                entity.setSecuritySeries(line[8]); // SctySrs
                entity.setExpiryDate(parseDate(line[9])); // XpryDt
                entity.setActualExpiryDate(parseDate(line[10])); // FininstrmActlXpryDt
                entity.setStrikePrice(parseBigDecimal(line[11])); // StrkPric
                entity.setOptionType(line[12]); // OptnTp
                entity.setInstrumentName(line[13]); // FinInstrmNm
                entity.setOpenPrice(parseBigDecimal(line[14])); // OpnPric
                entity.setHighPrice(parseBigDecimal(line[15])); // HghPric
                entity.setLowPrice(parseBigDecimal(line[16])); // LwPric
                entity.setClosePrice(parseBigDecimal(line[17])); // ClsPric
                entity.setLastPrice(parseBigDecimal(line[18])); // LastPric
                entity.setPreviousClosingPrice(parseBigDecimal(line[19])); // PrvsClsgPric
                entity.setUnderlyingPrice(parseBigDecimal(line[20])); // UndrlygPric
                entity.setSettlementPrice(parseBigDecimal(line[21])); // SttlmPric
                entity.setOpenInterest(parseLong(line[22])); // OpnIntrst
                entity.setChangeInOpenInterest(parseLong(line[23])); // ChngInOpnIntrst
                entity.setTotalTradingVolume(parseLong(line[24])); // TtlTradgVol
                entity.setTotalTradedValue(parseBigDecimal(line[25])); // TtlTrfVal
                entity.setTotalNumberOfTransactionsExecuted(parseLong(line[26])); // TtlNbOfTxsExctd
                entity.setSessionId(line[27]); // SsnId
                entity.setNewBoardLotQuantity(parseLong(line[28])); // NewBrdLotQty
                entity.setRemarks(line[29]); // Rmks
                entity.setReserved1(line[30]); // Rsvd1
                entity.setReserved2(line[31]); // Rsvd2
                entity.setReserved3(line[32]); // Rsvd3
                entity.setReserved4(line[33]); // Rsvd4
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());

                // Lookup stock by ISIN or symbol
                stockInfoRepository.findByIsinNumber(entity.getIsin())
                        .or(() -> stockInfoRepository.findBySymbol(entity.getTickerSymbol()))
                        .ifPresent(entity::setStock);

                if (entity.getStock() == null) {
                    System.out.println("⚠️ Warning: No stock found for ISIN " + entity.getIsin() +
                            " or Symbol " + entity.getTickerSymbol() +
                            " on date " + entity.getTradeDate());
                } else {
                    // Check for existing record to avoid duplicates
                    boolean exists = repository.existsByStockAndTradeDate(entity.getStock(), entity.getTradeDate());
                    if (exists) {
                        log.info("Info: Record already exists for {} on {}, skipping.", entity.getTickerSymbol(), entity.getTradeDate());
                        continue;
                    }
                    repository.save(entity);
                    log.info("✅ Saved price for {} on {} at price {}", entity.getTickerSymbol(), entity.getTradeDate(), entity.getClosePrice());
                }
            }
        }
    }

    // Utility methods for parsing
    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        value = value.trim();
        // Try yyyy-MM-dd, then dd-MMM-yyyy
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
