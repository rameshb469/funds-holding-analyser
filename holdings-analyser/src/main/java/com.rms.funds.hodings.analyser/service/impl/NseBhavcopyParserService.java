package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.entity.StockPriceHistory;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.repository.StockPriceHistoryRepository;
import com.rms.funds.hodings.analyser.service.BhavcopyParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;


// BhavcopyParserService.java

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.opencsv.CSVReader;

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
                String symbol = line[0].trim();
                String series = line[1].trim();
                BigDecimal closePrice = new BigDecimal(line[5]);
                Long volume = Long.parseLong(line[8]);
                BigDecimal turnover = new BigDecimal(line[9]);
                LocalDate tradeDate = LocalDate.parse(line[10],

                 DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));

                stockInfoRepository.findBySymbol(symbol)
                        .ifPresent(stock -> {
                            StockPriceHistory entity = new StockPriceHistory();
                            entity.setStock(stock);
                            entity.setTradeDate(tradeDate);
                            entity.setClosePrice(closePrice);
                            entity.setVolume(volume);
                            entity.setTurnover(turnover);

                            repository.save(entity);
                        });
            }
        }
    }
}


