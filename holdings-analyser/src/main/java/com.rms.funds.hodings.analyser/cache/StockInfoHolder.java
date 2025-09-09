package com.rms.funds.hodings.analyser.cache;

import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class StockInfoHolder {

    private static final String FREE_CASH_ISIN_CODE = "FREE_CASH";

    private final StockInfoRepository stockInfoRepository;

    private static final Map<String, StockInfoEntity> groupByIsinNumber = new LinkedHashMap<>();

    /**
     * Query Stock info details by using ISIN Code
     *
     * @param isinCode The ISIN Code of stock
     * @return Stock information
     */
    public Optional<StockInfoEntity> getByIsinCode(String isinCode) {
        if (groupByIsinNumber.containsKey(isinCode)) {
            return Optional.of(groupByIsinNumber.get(isinCode));
        }

        return stockInfoRepository.findByIsinNumber(isinCode);
    }

    @PostConstruct
    public void load(){
        groupByIsinNumber.putAll(stockInfoRepository.findAll().stream()
                .collect(toMap(StockInfoEntity::getIsinNumber, stockInfoEntity -> stockInfoEntity)));
        supportOldCodes();
    }

    public Optional<StockInfoEntity> getFreeCashStockInfo() {
        return stockInfoRepository.findByIsinNumber(FREE_CASH_ISIN_CODE);
    }

    public void supportOldCodes() {
        // Nestle changed its isin number
        groupByIsinNumber.put("INE239A01016", groupByIsinNumber.get("INE239A01024"));
        groupByIsinNumber.put("INE047A20013", groupByIsinNumber.get("INE047A01021"));
        groupByIsinNumber.put("INE192A20017", groupByIsinNumber.get("INE192A01025"));
        groupByIsinNumber.put("INE047A01039", groupByIsinNumber.get("INE047A01021"));
        groupByIsinNumber.put("INE262H01021", groupByIsinNumber.get("INE262H01013"));
        groupByIsinNumber.put("INE476A01022", groupByIsinNumber.get("INE476A01014"));
        groupByIsinNumber.put("INE200M01039", groupByIsinNumber.get("INE200M01021"));
        groupByIsinNumber.put("INE171Z01026", groupByIsinNumber.get("INE171Z01018"));

        groupByIsinNumber.put("INE457L01029", groupByIsinNumber.get("INE457L01011"));
        groupByIsinNumber.put("INE999A01023", groupByIsinNumber.get("INE999A01015"));
        groupByIsinNumber.put("INE205B01031", groupByIsinNumber.get("INE205B01023"));
        //INE542W01025 --> INE542W01017
        groupByIsinNumber.put("INE542W01025", groupByIsinNumber.get("INE542W01017"));
        //INE464A01036 --> INE464A01028
        groupByIsinNumber.put("INE464A01036", groupByIsinNumber.get("INE464A01028"));
        //INE932X01026 --> INE932X01018
        groupByIsinNumber.put("INE932X01026", groupByIsinNumber.get("INE932X01018"));
        // INE967H01025 --> INE967H01017
        groupByIsinNumber.put("INE967H01025", groupByIsinNumber.get("INE967H01017"));
        // INE806T01020 --> INE806T01012
        groupByIsinNumber.put("INE806T01020", groupByIsinNumber.get("INE806T01012"));
        // INE0Q3R04012 -->. INE0Q3R01026
        groupByIsinNumber.put("INE0Q3R04012", groupByIsinNumber.get("INE0Q3R01026"));
        //INE054301028 --> INE0J5401028
        groupByIsinNumber.put("INE054301028", groupByIsinNumber.get("INE0J5401028"));

        //  INE089A01031 --> INE089A01023 dr. reddy
        groupByIsinNumber.put("INE089A01031", groupByIsinNumber.get("INE089A01023"));

        groupByIsinNumber.put("INE628A20010", groupByIsinNumber.get("INE628A01036"));
        groupByIsinNumber.put("INE721A01047", groupByIsinNumber.get("INE721A01013"));

        // INE296A01032 --> INE296A01024
        // INE813A20018 --> INE813A01018
        groupByIsinNumber.put("INE296A01032", groupByIsinNumber.get("INE296A01024"));
        groupByIsinNumber.put("INE813A20018", groupByIsinNumber.get("INE813A01018"));
        groupByIsinNumber.put("INE663F01032", groupByIsinNumber.get("INE663F01024"));

        groupByIsinNumber.put("INE00FF01025", groupByIsinNumber.get("INE00FF01017"));

        //INE850M01015 --> INE027H01010
        groupByIsinNumber.put("INE850M01015", groupByIsinNumber.get("INE027H01010"));
       // INE249Z01020 --> INE249Z01012
        groupByIsinNumber.put("INE249Z01020", groupByIsinNumber.get("INE249Z01012"));
        //INE457L01029 --> INE457L01011
        groupByIsinNumber.put("INE457L01029", groupByIsinNumber.get("INE457L01011"));
        //INE180C01042 --> INE180C01026
        groupByIsinNumber.put("INE180C01042", groupByIsinNumber.get("INE180C01026"));
        //INE464A01036 --> INE464A01028
        groupByIsinNumber.put("INE464A01036", groupByIsinNumber.get("INE464A01028"));
        //INE0L9R01028 --> INE363A01022
        groupByIsinNumber.put("INE0L9R01028", groupByIsinNumber.get("INE363A01022"));
        //INE476A01022 --> INE476A01014
        groupByIsinNumber.put("INE476A01022", groupByIsinNumber.get("INE476A01014"));
        //INE949H13010--> INE949H01023
        groupByIsinNumber.put("INE949H13010", groupByIsinNumber.get("INE949H01023"));

        //INE324A01024 --> INE324A01032
        groupByIsinNumber.put("INE324A01032", groupByIsinNumber.get("INE324A01024"));
        //INE591G01017 ---> INE591G01025
        groupByIsinNumber.put("INE591G01025", groupByIsinNumber.get("INE591G01017"));

        // INE085J01014 --> INE085J20014
        groupByIsinNumber.put("INE085J20014", groupByIsinNumber.get("INE085J01014"));

        //INE774D01024 --> INE774D20024
        groupByIsinNumber.put("INE774D20024", groupByIsinNumber.get("INE774D01024"));

        //INE177H01021 --> INE177H01039
        groupByIsinNumber.put("INE177H01039", groupByIsinNumber.get("INE177H01021"));

        //INE386A01015 --> INE386A01023
        groupByIsinNumber.put("INE386A01023", groupByIsinNumber.get("INE386A01015"));
        //INE703F01010 --> INE302A01020
        groupByIsinNumber.put("INE703F01010", groupByIsinNumber.get("INE302A01020"));
        // INE775A08105--> INE775A01035
        groupByIsinNumber.put("INE775A08105", groupByIsinNumber.get("INE775A01035"));

        // INE545A01024 --> INE545A01016
        groupByIsinNumber.put("INE545A01024", groupByIsinNumber.get("INE545A01016"));

        // INE134E08MA1 -> INE134E01011
        groupByIsinNumber.put("INE134E08MA1", groupByIsinNumber.get("INE134E01011"));

        // INE766P20016 --> INE766P01016
        groupByIsinNumber.put("INE766P20016", groupByIsinNumber.get("INE766P01016"));

        // FREE_CASH --> INE020B08FJ3
        groupByIsinNumber.put("INE020B08FJ3", groupByIsinNumber.get("FREE_CASH"));


        // FREE_CASH --> INE1C3207081
        groupByIsinNumber.put("INE1C3207081", groupByIsinNumber.get("FREE_CASH"));
        // FREE_CASH --> https://nsdl.co.in/master_search_detail_res.php?isin=INE059B13029
        groupByIsinNumber.put("INE059B13029", groupByIsinNumber.get("FREE_CASH"));
        // INE495B13025
        groupByIsinNumber.put("INE495B13025", groupByIsinNumber.get("FREE_CASH"));
        //INE280B01018
        groupByIsinNumber.put("INE280B01018", groupByIsinNumber.get("FREE_CASH"));
        //INE1JAR25012
        groupByIsinNumber.put("INE1JAR25012", groupByIsinNumber.get("FREE_CASH"));
        //INE0GGX23010
        groupByIsinNumber.put("INE0GGX23010", groupByIsinNumber.get("FREE_CASH"));
        //INE494B04019
        groupByIsinNumber.put("INE494B04019", groupByIsinNumber.get("FREE_CASH"));
        //INE122R01018
        groupByIsinNumber.put("INE122R01018", groupByIsinNumber.get("FREE_CASH"));
        //INE0H7R23014
        groupByIsinNumber.put("INE0H7R23014", groupByIsinNumber.get("FREE_CASH"));
        //INE0NR623014
        groupByIsinNumber.put("INE0NR623014", groupByIsinNumber.get("FREE_CASH"));

        List.of("INE671B01034", "INE549I01011", "INE066P20011", "INE643A20019").forEach(stock -> {
            groupByIsinNumber.put(stock, groupByIsinNumber.get("FREE_CASH"));
        });








    }
}
