package com.rms.funds.hodings.analyser.utility;

import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class DateUtil {
    private static final String DATE_MAPPER_1 = "DATE_MAPPER_1";

    private static final String DATE_MAPPER_2 = "DATE_MAPPER_2";

    private static final String DATE_MAPPER_3 = "DATE_MAPPER_3";

    private static final String DATE_MAPPER_4 = "DATE_MAPPER_4";

    private static final String CURRENT_DATE_KEY = "CURRENT_DATE_KEY";

    private static final String DDN = "ddN";
    private static Map<String, DateTimeFormatter> SimpleDateFormatMap = new HashMap<>();

    public static List<Pair<String, LocalDate>> getDownloadLinks(MutualFundConfigEntity config) {
        String downloadUrl = config.getDownloadUrl();
        if (downloadUrl.contains("${") && downloadUrl.contains("}")) {
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            LocalDate last1Year = LocalDate.now().minusYears(1);
//            LocalDate last1Year = LocalDate.now().minusMonths(2);

            List<Map<String, Object>> placeholderList = new ArrayList<>();
            while (!lastMonth.equals(last1Year)) {
                Map<String, Object> placeholder = new HashMap<>();
                LocalDate lastDayOfMonth = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
                placeholder.put(DATE_MAPPER_1, getDateValue(config.getDateMapper1(),
                        lastDayOfMonth, config.getDateMapper1Config()));
                placeholder.put(DATE_MAPPER_2, getDateValue(config.getDateMapper2(),
                        lastDayOfMonth, config.getDateMapper2Config()));
                placeholder.put(DATE_MAPPER_3, getDateValue(config.getDateMapper3(),
                        lastDayOfMonth, config.getDateMapper3Config()));
                placeholder.put(DATE_MAPPER_4, getDateValue(config.getDateMapper4(),
                        lastDayOfMonth, config.getDateMapper4Config()));
                placeholder.put(CURRENT_DATE_KEY, lastDayOfMonth);
                placeholderList.add(placeholder);

                lastMonth = lastMonth.minusMonths(1);
            }

            List<Pair<String, LocalDate>> downloadUrlList = new ArrayList<>();
            for (Map<String, Object> placeholder : placeholderList) {
                StringSubstitutor stringSubstitutor = new StringSubstitutor(placeholder, "${", "}");
                downloadUrlList.add(Pair.of(stringSubstitutor.replace(downloadUrl), (LocalDate) placeholder.get(CURRENT_DATE_KEY)));
            }

            return downloadUrlList;
        }
        return List.of(Pair.of(downloadUrl, LocalDate.now().minusMonths(1)));
    }

    private static String getDateValue(String pattern, LocalDate date, String dateConfig) {

        if (!StringUtils.isEmpty(pattern)) {

            boolean requiredDayTag = pattern.contains(DDN);
            if (requiredDayTag) {
                pattern = pattern.replace(DDN, "dd");
            }
            String finalPattern = pattern;
            DateTimeFormatter simpleDateFormat = SimpleDateFormatMap
                    .computeIfAbsent(finalPattern, value -> DateTimeFormatter.ofPattern(finalPattern));
            if (!StringUtils.isEmpty(dateConfig)) {
                date = applyConfig(date, dateConfig);
            }

            String dateValue = simpleDateFormat.format(date);
            if (requiredDayTag) {
                return getDayOfMonthSuffix(dateValue.substring(0, 2)) + dateValue.substring(2);
            }

            return dateValue;
        }
        return null;
    }

    private static LocalDate applyConfig(LocalDate date, String dateConfig) {
        String[] configs = dateConfig.split(",");
        for (String config : configs) {
            if (config.contains("D")) {
                date = date.plusDays(Long.parseLong(config.replace("D", "")));
            } else if (config.contains("M")) {
                date = date.plusMonths(Long.parseLong(config.replace("M", "")));
            } else if (config.contains("Y")) {
                date = date.plusYears(Long.parseLong(config.replace("Y", "")));
            }
        }
        return date;
    }

    private static String getDayOfMonthSuffix(final String value) {
        Integer n = Integer.parseInt(value);
        if (n >= 11 && n <= 13) {
            return value + "th";
        }
        switch (n % 10) {
            case 1:
                return value + "st";
            case 2:
                return value + "nd";
            case 3:
                return value + "rd";
            default:
                return value + "th";
        }
    }

    /**
     * Get the last date of month for given value
     *
     * @param pattern pattern of date patter
     * @param lastMontCount from current month to current-month - last mont
     * @return list of date values
     */
    public static Map<String, LocalDate> getLastDayOfMonth(String pattern, int lastMontCount){
        Map<String,LocalDate> results = new LinkedHashMap<>();
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        DateTimeFormatter simpleDateFormat = SimpleDateFormatMap
                .computeIfAbsent(pattern, value -> DateTimeFormatter.ofPattern(pattern));
        while (lastMontCount-- > 0){
            final LocalDate lastDayOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
            results.put(simpleDateFormat.format(lastDayOfLastMonth), lastDayOfLastMonth);
            lastMonth = lastMonth.minusMonths(1);
        }
        return results;
    }
}
