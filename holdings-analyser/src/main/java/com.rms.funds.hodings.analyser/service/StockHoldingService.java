package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;

import java.time.LocalDate;

public interface StockHoldingService {

    StockHoldingDto getMetrics(Long stockId);

    HoldingChangeMetricDto getHoldingChangeMetrics(LocalDate date);

}
