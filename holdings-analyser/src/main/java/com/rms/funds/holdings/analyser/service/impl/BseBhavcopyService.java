package com.rms.funds.holdings.analyser.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import com.rms.funds.holdings.analyser.service.BhavcopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service("bseBhavCopyService")
@RequiredArgsConstructor
public class BseBhavcopyService implements BhavcopyService {

    private final StockInfoRepository stockRepo;
    private final StockPriceHistoryRepository priceRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void fetchAndStoreBhavcopy(LocalDate date) throws Exception {
        String dateStr = date.format(DateTimeFormatter.ofPattern("ddMMyy"));

        ProcessBuilder pb = new ProcessBuilder("python3",
                "src/main/resources/scripts/bse_bhavcopy_fetch.py", dateStr);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("Python script failed");

            List<Map<String, Object>> prices = objectMapper.readValue(
                    output.toString(),
                    new TypeReference<>() {}
            );

            for (Map<String, Object> record : prices) {
                String symbol = record.get("symbol").toString();
                StockInfoEntity stock = stockRepo.findBySymbol(symbol).orElse(null);
                if (stock == null) continue;

                StockPriceHistory price = new StockPriceHistory();
                price.setStock(stock);
                price.setTradeDate(LocalDate.parse(record.get("date").toString()));
//                price.setOpenPrice(new BigDecimal(record.get("open").toString()));
//                price.setHighPrice(new BigDecimal(record.get("high").toString()));
//                price.setLowPrice(new BigDecimal(record.get("low").toString()));
                price.setClosePrice(new BigDecimal(record.get("close").toString()));
                price.setVolume(Long.parseLong(record.get("volume").toString()));

                priceRepo.save(price);
            }
        }
    }
}

