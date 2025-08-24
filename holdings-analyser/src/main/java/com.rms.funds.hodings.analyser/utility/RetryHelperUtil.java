package com.rms.funds.hodings.analyser.utility;

import com.rms.funds.hodings.analyser.model.ExcelDownloaderAttributes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RetryHelperUtil {

    private static final String TATA =  "TATA";
    private static final String MIRAE_MF = "MIRAE";
    private static final String UNION_MF = "UNION";
    private static final String QUANT = "QUANT";
    private static final String HSBC = "HSBC";
    public static final String NIPPON = "NIPPON";
    public static final String CR = "CR";

    private static final Map<String, String> updateLinks = new HashMap<>();

    private static Set<String> retryMutualFunds = new HashSet<>();

    static {
        retryMutualFunds.add(TATA);
        retryMutualFunds.add(MIRAE_MF);
        retryMutualFunds.add(UNION_MF);
        retryMutualFunds.add(QUANT);
        retryMutualFunds.add(HSBC);
        retryMutualFunds.add(NIPPON);
        retryMutualFunds.add(CR);

        updateLinks.put("https://betacms.tatamutualfund.com/system/files/2024-09/Monthly Portfolio as on 31st August 2024.xlsx",
                "https://betacms.tatamutualfund.com/system/files/2024-09/Monthly Portfolio as on 30th August 2024.xlsx");

        updateLinks.put("https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-31-Aug-24.xls",
                "https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF_MONTHLY_PORTFOLIO_31-Aug-24.xls");

        updateLinks.put("https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-31-Jan-25.xls",
                "https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF_MONTHLY_PORTFOLIO_31-Jan-25.xls");

        updateLinks.put("https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-30092024/hsbc-large-mid-cap-fund-30-sep-2024.xlsx",
                "https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-30092024/hsbc-large-and-mid-cap-fund-30-september-2024.xlsx");
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

        if (QUANT.equalsIgnoreCase(mutualFundHouseName)){

            if (url.contains("Jun")){
                url = url.replace("Jun", "June");
            }

            if (url.contains("Jul")){
                url = url.replace("Jul", "July");
            }

            if (url.contains("Sept")){
                url = url.replace("Sept","Sep");
            }

            return Pair.of(url, attributes);
        }

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

            if (url.contains("miccf_feb2025")){
                url = url.replace("miccf_feb2025", "miicf_feb2025");
            }

            if (url.contains("_may2025")){
                url = url.replace("_may2025", "-may2025");
            }

            if (url.contains("mar2025")) {
                url = url.replace("mar2025", "march2025");
            }

            if (url.contains("sep2024")) {
                url = url.replace("sep2024", "sept2024");
            }

            if (url.contains("aug2024")) {
                url = url.replace("aug2024", "31aug2024");
            }

            if (url.contains("_jul")) {
                url = url.replace("_jul", "-july");
            }

            if (url.contains("jun")) {
                url = url.replace("jun", "june");
            }
        }

        if (UNION_MF.equalsIgnoreCase(mutualFundHouseName)){
            url = url.toLowerCase();
        }

        if (HSBC.equalsIgnoreCase(mutualFundHouseName)){

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (updateLinks.containsKey(url)) {
                url = updateLinks.get(url);
            } else {
                url = url.toLowerCase();
            }
        }

        if (NIPPON.equalsIgnoreCase(mutualFundHouseName)) {

            if (url.contains("Jun")){
                url = url.replace("Jun", "June");
            }

            if (url.contains("Jul")){
                url = url.replace("Jul", "July");
            }

            if (url.contains("Apr")){
                url = url.replace("Apr", "April");
            }

            if (url.contains("30-Nov-24")) {
                url = url.replace("30-Nov-24", "30-Nov-2024");
            }

            if (url.contains("Sept")){
                url = url.replace("Sept","Sep");
            }

            if (updateLinks.containsKey(url)) {
                url = updateLinks.get(url);
            }
        }

        if (CR.equalsIgnoreCase(mutualFundHouseName)){
            url = url.toLowerCase();
        }

        return Pair.of(url, attributes);
    }
}
