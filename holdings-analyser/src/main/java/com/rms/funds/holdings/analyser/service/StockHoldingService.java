package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.holdings.analyser.controller.dto.StockHoldingDto;

import java.time.LocalDate;

public interface StockHoldingService {

    StockHoldingDto getMetrics(Long stockId);

    HoldingChangeMetricDto getHoldingChangeMetrics(LocalDate date);

}
