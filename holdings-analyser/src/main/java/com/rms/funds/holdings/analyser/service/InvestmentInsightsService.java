package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.InvestmentInsightsResponse;
import com.rms.funds.holdings.analyser.model.HoldingChangeMetricFilter;

import java.time.LocalDate;

public interface InvestmentInsightsService {

    InvestmentInsightsResponse getInsights(HoldingChangeMetricFilter filter);
}
