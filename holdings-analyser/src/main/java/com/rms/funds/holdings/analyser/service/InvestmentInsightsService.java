package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.InvestmentInsightsResponse;

import java.time.LocalDate;

public interface InvestmentInsightsService {

    InvestmentInsightsResponse getInsights(LocalDate asOfDate);
}
