package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.StockHoldingDto;

public interface StockHoldingService {

    StockHoldingDto getMetrics(Long stockId);
}
