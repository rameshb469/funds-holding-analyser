package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.dto.BacktestEntryDto;
import com.rms.funds.holdings.analyser.dto.StockVolumeReportDto;
import com.rms.funds.holdings.analyser.entity.StockPriceHistory;
import com.rms.funds.holdings.analyser.repository.StockPriceHistoryRepository;
import com.rms.funds.holdings.analyser.service.impl.StockReportServiceImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class StockReportController {

    private final StockReportServiceImpl reportService;
    private final StockPriceHistoryRepository historyRepo;

    public StockReportController(StockReportServiceImpl reportService, StockPriceHistoryRepository historyRepo) {
        this.reportService = reportService;
        this.historyRepo = historyRepo;
    }

    @GetMapping("/volume")
    public ResponseEntity<List<StockVolumeReportDto>> volumeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<StockVolumeReportDto> r = reportService.generateVolumeReport(start, end);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/volume/last-months")
    public ResponseEntity<List<StockVolumeReportDto>> volumeReportLastMonths(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "3") int months) {
        LocalDate actualEnd = end != null ? end : LocalDate.now();
        actualEnd = actualEnd.withDayOfMonth(1).minusDays(1); // end of previous month₹
        actualEnd = actualEnd.minusMonths(months - 1)
                .withDayOfMonth(actualEnd.minusMonths(months - 1).lengthOfMonth());
        List<StockVolumeReportDto> r = reportService.generateVolumeReport(actualEnd, actualEnd);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/backtest")
    public ResponseEntity<List<BacktestEntryDto>> backtest(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "3") int windowMonths,
            @RequestParam(defaultValue = "10") int topN,
            @RequestParam(required = false) Double tradeCostPctPerSide) {
        List<BacktestEntryDto> r;
        if (tradeCostPctPerSide != null) {
            r = reportService.backtestTopN(from, to, windowMonths, topN, tradeCostPctPerSide);
        } else {
            r = reportService.backtestTopN(from, to, windowMonths, topN);
        }
        return ResponseEntity.ok(r);
    }

    // Debug endpoint to diagnose empty reports: returns row count and a small sample
    @GetMapping("/debug/volume")
    public ResponseEntity<Map<String, Object>> debugVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<StockPriceHistory> rows = historyRepo.findByTradeDateBetween(start, end);
        Map<String, Object> res = new HashMap<>();
        res.put("rowsInRange", rows.size());

        List<Map<String, Object>> sample = rows.stream().limit(20).map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("tradeDate", r.getTradeDate());
            m.put("ticker", r.getTickerSymbol());
            m.put("stockId", r.getStock() != null ? r.getStock().getId() : null);
            m.put("closePrice", r.getClosePrice());
            return m;
        }).collect(Collectors.toList());
        res.put("sample", sample);

        // also return total count of history rows in DB (may be expensive on very large tables)
        try {
            long total = historyRepo.count();
            res.put("totalHistoryRows", total);
        } catch (Exception ex) {
            res.put("totalHistoryRows", "unavailable");
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/backtest/detail")
    public ResponseEntity<List<Map<String, Object>>> backtestDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "3") int windowMonths,
            @RequestParam(defaultValue = "10") int topN,
            @RequestParam(required = false) Double tradeCostPctPerSide) {

        double perSide = tradeCostPctPerSide != null ? tradeCostPctPerSide : 0.001; // default 0.1%
        double roundTripCost = 1 - (1 - perSide) * (1 - perSide);

        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate windowStart = from;

        while (!windowStart.plusMonths(windowMonths).isAfter(to)) {
            LocalDate windowEnd = windowStart.plusMonths(windowMonths).minusDays(1);
            LocalDate holdStart = windowEnd.plusDays(1);
            LocalDate holdEnd = windowEnd.plusMonths(1);

            // generate report for window
            List<StockVolumeReportDto> reports = reportService.generateVolumeReport(windowStart, windowEnd);
            reports.sort(Comparator.comparingDouble(r -> {
                double std = r.getVolumeStdDev();
                return -(std <= 0 ? r.getAvgVolume() : r.getAvgVolume() / std);
            }));

            List<StockVolumeReportDto> picks = reports.stream().limit(topN).collect(Collectors.toList());

            // forward rows
            List<StockPriceHistory> forwardRows = historyRepo.findByTradeDateBetween(holdStart, holdEnd);
            Map<Long, java.math.BigDecimal> forwardCloseById = forwardRows.stream()
                    .filter(r -> r.getStock() != null && r.getClosePrice() != null)
                    .collect(Collectors.groupingBy(r -> r.getStock().getId(),
                            Collectors.collectingAndThen(
                                    Collectors.maxBy(Comparator.comparing(StockPriceHistory::getTradeDate)),
                                    opt -> opt.map(StockPriceHistory::getClosePrice).orElse(java.math.BigDecimal.ZERO)
                            )));
            Map<String, java.math.BigDecimal> forwardCloseByTicker = forwardRows.stream()
                    .filter(r -> r.getTickerSymbol() != null && r.getClosePrice() != null)
                    .collect(Collectors.groupingBy(StockPriceHistory::getTickerSymbol,
                            Collectors.collectingAndThen(
                                    Collectors.maxBy(Comparator.comparing(StockPriceHistory::getTradeDate)),
                                    opt -> opt.map(StockPriceHistory::getClosePrice).orElse(java.math.BigDecimal.ZERO)
                            )));

            int picksWithData = 0;
            List<Map<String, Object>> pickDetails = new ArrayList<>();
            List<Double> grossReturns = new ArrayList<>();
            List<Double> netReturns = new ArrayList<>();

            for (StockVolumeReportDto p : picks) {
                Map<String, Object> pm = new HashMap<>();
                pm.put("ticker", p.getTicker());
                pm.put("stockId", p.getStockId());
                pm.put("avgVolume", p.getAvgVolume());
                pm.put("volumeStdDev", p.getVolumeStdDev());
                double score = (p.getVolumeStdDev() <= 0) ? p.getAvgVolume() : p.getAvgVolume() / p.getVolumeStdDev();
                pm.put("score", score);
                pm.put("lastClose", p.getLastClosePrice());

                java.math.BigDecimal fClose = null;
                if (p.getStockId() != null) fClose = forwardCloseById.get(p.getStockId());
                if (fClose == null && p.getTicker() != null) fClose = forwardCloseByTicker.get(p.getTicker());

                pm.put("forwardClose", fClose);
                if (p.getLastClosePrice() != null && fClose != null && fClose.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    java.math.BigDecimal ret = fClose.subtract(p.getLastClosePrice())
                            .divide(p.getLastClosePrice(), 6, java.math.RoundingMode.HALF_UP);
                    pm.put("forwardReturn", ret);
                    double gross = ret.doubleValue();
                    double net = (1 + gross) * (1 - roundTripCost) - 1;
                    pm.put("grossReturn", gross);
                    pm.put("netReturn", net);
                    grossReturns.add(gross);
                    netReturns.add(net);
                    picksWithData++;
                } else {
                    pm.put("forwardReturn", null);
                    pm.put("grossReturn", null);
                    pm.put("netReturn", null);
                }

                pickDetails.add(pm);
            }

            // compute aggregates
            Map<String, Object> aggregates = new HashMap<>();
            aggregates.put("meanGross", mean(grossReturns));
            aggregates.put("medianGross", median(grossReturns));
            aggregates.put("stdDevGross", stddev(grossReturns));
            aggregates.put("hitRateGross", hitRate(grossReturns));
            aggregates.put("meanNet", mean(netReturns));
            aggregates.put("medianNet", median(netReturns));
            aggregates.put("stdDevNet", stddev(netReturns));
            aggregates.put("hitRateNet", hitRate(netReturns));

            Map<String, Object> windowMap = new HashMap<>();
            windowMap.put("windowStart", windowStart.toString());
            windowMap.put("windowEnd", windowEnd.toString());
            windowMap.put("picksSelected", picks.size());
            windowMap.put("picksWithForwardData", picksWithData);
            windowMap.put("aggregates", aggregates);
            windowMap.put("picks", pickDetails);

            results.add(windowMap);

            windowStart = windowStart.plusMonths(1);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/backtest/portfolio")
    public ResponseEntity<Map<String, Object>> backtestPortfolio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "3") int windowMonths,
            @RequestParam(defaultValue = "10") int topN,
            @RequestParam(required = false) Double tradeCostPctPerSide) {

        double perSide = tradeCostPctPerSide != null ? tradeCostPctPerSide : 0.001;
        Map<String, Object> res = reportService.backtestPortfolio(from, to, windowMonths, topN, perSide);
        return ResponseEntity.ok(res);
    }

    // helper stats
    private static Double mean(List<Double> vals) {
        if (vals == null || vals.isEmpty()) return null;
        return vals.stream().mapToDouble(Double::doubleValue).average().orElse(0d);
    }

    private static Double median(List<Double> vals) {
        if (vals == null || vals.isEmpty()) return null;
        List<Double> s = new ArrayList<>(vals);
        Collections.sort(s);
        int n = s.size();
        if (n % 2 == 1) return s.get(n / 2);
        return (s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
    }

    private static Double stddev(List<Double> vals) {
        if (vals == null || vals.size() <= 1) return 0d;
        double m = mean(vals);
        double sum = 0d;
        for (double v : vals) sum += (v - m) * (v - m);
        return Math.sqrt(sum / vals.size());
    }

    private static Double hitRate(List<Double> vals) {
        if (vals == null || vals.isEmpty()) return null;
        long pos = vals.stream().filter(v -> v > 0).count();
        return (double) pos / (double) vals.size();
    }
}
