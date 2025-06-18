package com.rms.funds.hodings.analyser.utility;

import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RetryHelperUtil {

    private static final String TATA =  "TATA";
    private static final String MIRAE_MF = "MIRAE";

    private static final Map<String, String> updateLinks = new HashMap<>();

    private static Set<String> retryMutualFunds = new HashSet<>();

    static {
        retryMutualFunds.add(TATA);
        retryMutualFunds.add(MIRAE_MF);

        updateLinks.put("https://betacms.tatamutualfund.com/system/files/2024-09/Monthly Portfolio as on 31st August 2024.xlsx",
                "https://betacms.tatamutualfund.com/system/files/2024-09/Monthly Portfolio as on 30th August 2024.xlsx");
    }

    public static boolean isRetryRequired(ExcelDownloaderAttributes attributes) {
        if (attributes != null) {
            return retryMutualFunds.contains(attributes.getMutualFundHouse());
        }
        return false;
    }

    public static Pair<String, ExcelDownloaderAttributes> updateAttributes(String url,
                                                                           ExcelDownloaderAttributes attributes){
        String mutualFundHouseName = attributes.getMutualFundHouse();

        if (TATA.equalsIgnoreCase(mutualFundHouseName)) {

            if (updateLinks.containsKey(url)) {
                url = updateLinks.get(url);
            } else {
                attributes =  attributes.toBuilder().extension("xls").build();
                url = url.replace("xlsx", "xls");
            }
        }

        if (MIRAE_MF.equalsIgnoreCase(mutualFundHouseName)){
            url = url.toLowerCase();

            if (url.contains("-may2025")){
                url = url.replace("-may2025", "_may2025");
            }

            if (url.contains("sep2024")) {
                url = url.replace("sep2024", "sept2024");
            }

            if (url.contains("aug2024")) {
                url = url.replace("aug2024", "31aug2024");
            }

            if (url.contains("jul")) {
                url = url.replace("jul", "july");
            }

            if (url.contains("jun")) {
                url = url.replace("jun", "june");
            }
        }

        return Pair.of(url, attributes);
    }
}
