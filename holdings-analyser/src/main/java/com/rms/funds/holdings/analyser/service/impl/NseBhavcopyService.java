package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.entity.StockInfoEntity;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.model.BhavcopyRecord;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import com.rms.funds.holdings.analyser.service.BhavcopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service("nseBhavCopyService")
@RequiredArgsConstructor
public class NseBhavcopyService implements BhavcopyService {
    private final StockInfoRepository stockRepo;
    private final StockPriceHistoryRepository priceRepo;

    @Override
    public void fetchAndStoreBhavcopy(LocalDate date) throws Exception {
        String datUrl = "https://nsearchives.nseindia.com/content/trdops/FCM_INTRM_BC" +
                date.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".DAT";

        try{
            String datContent = downloadDatFile(datUrl);
            for (BhavcopyRecord record : parseBhavcopy(datContent, date)) {
                StockInfoEntity stock = stockRepo.findBySymbol(record.getSymbol()).orElse(null);
                if (stock == null) continue;

                LocalDate tradeDate = LocalDate.parse(record.getDate());
                if (!priceRepo.existsByStockIdAndTradeDate(stock.getId(), tradeDate)) {
                    StockPriceHistory price = new StockPriceHistory();
                    price.setStock(stock);
                    price.setTradeDate(LocalDate.parse(record.getDate()));
                    price.setOpenPrice(new BigDecimal(record.getOpen()));
                    price.setHighPrice(new BigDecimal(record.getHigh()));
                    price.setLowPrice(new BigDecimal(record.getLow()));
                    price.setClosePrice(new BigDecimal(record.getClose()));
                    price.setVolume(Long.parseLong(record.getVolume()));
                    price.setNumberOfTrades(Long.parseLong(record.getNumberOfTrades()));
                    price.setCreatedAt(LocalDateTime.now());
                    priceRepo.save(price);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String downloadDatFile(String datUrl) throws Exception {
        URL url = new URL(datUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) { sb.append(line).append("\n"); }
        }
        return sb.toString();
    }

    private List<BhavcopyRecord> parseBhavcopy(String datContent, LocalDate date) {
        List<BhavcopyRecord> result = new ArrayList<>();
        for (String line : datContent.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(",");
            BhavcopyRecord record = new BhavcopyRecord();
            // Note: Index 0 is full name (may or may not be used)
            record.setSymbol(fields[1].trim());
            record.setSeries(fields[2].trim());
            record.setOpen(fields[4].trim());
            record.setHigh(fields[5].trim());
            record.setLow(fields[6].trim());
            record.setClose(fields[7].trim());
            // Volume and numberOfTrades often blank, so default to 0 if empty
            String trades = (fields.length > 10 && !fields[10].trim().isEmpty()) ? fields[10].trim() : "0";
            String volume = (fields.length > 11 && !fields[11].trim().isEmpty()) ? fields[11].trim() : "0";
            record.setNumberOfTrades(trades);
            record.setVolume(volume);
            // Turnover value might be zero or valid number at 12th index
            record.setValueOfSharesTraded(fields[12].trim());
            record.setDate(date.toString());
            result.add(record);
        }
        return result;
    }

}
