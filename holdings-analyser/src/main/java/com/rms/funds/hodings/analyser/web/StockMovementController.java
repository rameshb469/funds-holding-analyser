package com.rms.funds.hodings.analyser.web;

import com.rms.funds.hodings.analyser.service.impl.StockMovementResult;
import com.rms.funds.hodings.analyser.service.impl.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService movementService;

    @GetMapping
    public ResponseEntity<List<StockMovementResult>> getMovements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String csvSource
    ) throws Exception {
        List<StockMovementResult> results = movementService.computeMovements(date, csvSource);
        return ResponseEntity.ok(results);
    }
}

