package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.HoldingChangeMetricDto;
import com.rms.funds.holdings.analyser.controller.dto.StockHoldingDto;
import com.rms.funds.holdings.analyser.model.HoldingChangeMetricFilter;

public interface StockHoldingService {

    StockHoldingDto getMetrics(Long stockId);

    HoldingChangeMetricDto getHoldingChangeMetrics(HoldingChangeMetricFilter filter);

}
