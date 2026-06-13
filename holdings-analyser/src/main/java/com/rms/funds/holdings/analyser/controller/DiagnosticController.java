package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.entity.MfHoldingEntity;
import com.rms.funds.holdings.analyser.repository.MfHoldingRepository;
import com.rms.funds.holdings.analyser.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DiagnosticController {

    private final MfHoldingRepository mfHoldingRepository;
    private final StockInfoRepository stockInfoRepository;

    @GetMapping("/available-dates")
    public Map<String, Object> getAvailableDates() {
        Map<String, Object> result = new HashMap<>();

        // Get all distinct dates from holdings
        List<MfHoldingEntity> allHoldings = mfHoldingRepository.findAll();
        Set<LocalDate> dates = new TreeSet<>(Collections.reverseOrder());
        for (MfHoldingEntity h : allHoldings) {
            if (h.getAtDate() != null) {
                dates.add(h.getAtDate());
            }
        }

        result.put("available_dates", dates);
        result.put("total_holdings_count", allHoldings.size());
        result.put("unique_date_count", dates.size());

        if (!dates.isEmpty()) {
            result.put("latest_date", dates.iterator().next());
            result.put("oldest_date", dates.stream().reduce((a, b) -> b).orElse(null));
        }

        return result;
    }

    @GetMapping("/holdings-for-date")
    public Map<String, Object> getHoldingsForDate(
            @RequestParam(name = "date") String dateStr,
            @RequestParam(name = "marketCapCategory", required = false) String marketCapCategory) {

        Map<String, Object> result = new HashMap<>();
        try {
            LocalDate date = LocalDate.parse(dateStr);

            List<MfHoldingEntity> holdings = mfHoldingRepository.findByAtDate(date);
            result.put("date", date);
            result.put("total_holdings_for_date", holdings.size());

            // Count by marketCapCategory
            Map<String, Long> capCounts = new HashMap<>();
            for (MfHoldingEntity h : holdings) {
                if (h.getStockInfoEntity() != null) {
                    String cap = h.getStockInfoEntity().getMarketCapCategory();
                    capCounts.put(cap != null ? cap : "NULL", capCounts.getOrDefault(cap, 0L) + 1);
                }
            }
            result.put("holdings_by_market_cap_category", capCounts);

            // Check if stocks have sector/industry
            long withoutSector = 0, withoutIndustry = 0, withBoth = 0;
            for (MfHoldingEntity h : holdings) {
                if (h.getStockInfoEntity() != null) {
                    boolean hasSector = h.getStockInfoEntity().getSector() != null;
                    boolean hasIndustry = h.getStockInfoEntity().getIndustry() != null;
                    if (hasSector && hasIndustry) {
                        withBoth++;
                    } else if (!hasSector) {
                        withoutSector++;
                    } else if (!hasIndustry) {
                        withoutIndustry++;
                    }
                }
            }
            result.put("stocks_with_both_sector_industry", withBoth);
            result.put("stocks_without_sector", withoutSector);
            result.put("stocks_without_industry", withoutIndustry);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    @GetMapping("/stock-count")
    public Map<String, Object> getStockCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("total_stocks", stockInfoRepository.count());
        result.put("stocks_without_sector", stockInfoRepository.findByMarketCapCategoryIsNull().size());
        return result;
    }
}

