package com.rms.funds.hodings.analyser.test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.repository.IndustryRepository;
import com.rms.funds.hodings.analyser.repository.SectorRepository;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.rms.funds.hodings.analyser.utility.AppConst._100K;
import static com.rms.funds.hodings.analyser.utility.NumberUtil.safeInt;

//@Component
@RequiredArgsConstructor
@Slf4j
public class StockLoaderService implements CommandLineRunner {

    private static final Long default_sector_id = 38L;
    private static final Long default_industry_id = 180L;


    private final StockInfoRepository stockInfoRepository;
    private final SectorRepository sectorRepository;
    private final IndustryRepository industryRepository;

    @Override
    public void run(String... args) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        File file = ResourceUtils.getFile("classpath:python/stock_details_enriched.json");
        List<StockInfo> list = objectMapper.readValue(file, new TypeReference<List<StockInfo>>() {});

        List<StockInfoEntity> updated = new ArrayList<>();
        int count = 0;
        for (StockInfo stock : list) {
            StockInfoEntity stockInfoEntity = stockInfoRepository.findBySymbol(stock.symbol)
                     .orElseGet(() -> stockInfoRepository.findByIsinNumber(stock.isin_number).orElse(null));
             if (stockInfoEntity != null) {
                 updated.add(stockInfoEntity.toBuilder()
                         .marketCap(stock.marketCap/_100K)
                         .sharesOutstanding(stock.sharesOutstanding)
                         .rank(stock.rank)
                         .marketCapCategory(stock.marketCapCategory)
                         .updatedAt(LocalDateTime.now())
                         .build());
                 count++;
             } else {
                 log.error("Missed stock update symbol : {} and isin : {}", stock.symbol, stock.isin_number);
             }

        }

        System.out.println("Total new records are : "+count);
        stockInfoRepository.saveAll(updated);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
class StockInfo {
    String symbol;
    String name_of_company;
    String series;
    @JsonFormat(pattern = "dd-MMM-yyyy")
    Date date_of_listing;
    String paid_up_value;
    String market_lot;
    String isin_number;
    String face_value;
    Long marketCap;
    Long sharesOutstanding;
    int rank;
    String marketCapCategory;
//    Timestamp created_at;
//    Timestamp updated_at;
}

//    "marketCap": 953688195072.0,
//            "sharesOutstanding": 9070650368.0,
//            "rank": 103,
//            "marketCapCategory": "MidCap"

//        for (StockInfo stock : list) {
//             Optional<StockInfoEntity> optionalStockInfo = stockInfoRepository.findBySymbol(stock.symbol);
//             if (optionalStockInfo.isPresent()) {
//                 updated.add(optionalStockInfo.get().toBuilder()
//                         .series(stock.series)
//                         .dateOfListing(stock.date_of_listing)
//                         .paidUpValue(safeInt(stock.paid_up_value).orElse(0))
//                         .marketLot(safeInt(stock.market_lot).orElse(0))
//                         .faceValue(safeInt(stock.face_value).orElse(0))
//                         .marketCap(Long.valueOf(safeInt(stock.market_lot).orElse(0)))
//                         .updatedAt(LocalDateTime.now())
//                         .build());
//             } else {
//                 updated.add(StockInfoEntity.builder()
//                         .symbol(stock.symbol)
//                         .series(stock.series)
//                         .company(stock.name_of_company)
//                         .dateOfListing(stock.date_of_listing)
//                         .paidUpValue(safeInt(stock.paid_up_value).orElse(0))
//                         .marketLot(safeInt(stock.market_lot).orElse(0))
//                         .isinNumber(stock.isin_number)
//                         .faceValue(safeInt(stock.face_value).orElse(0))
//                         .sector(sectorRepository.findById(default_sector_id).orElse(null))
//                         .industry(industryRepository.findById(default_industry_id).orElse(null))
//                         .marketCap(Long.valueOf(safeInt(stock.market_lot).orElse(0)))
//                         .createdAt(LocalDateTime.now())
//                         .updatedAt(LocalDateTime.now())
//                         .build());
//                 count++;
//             }
//
//        }