package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.InvestmentInsightsResponse;

import java.time.LocalDate;

public interface InvestmentInsightsService {

    InvestmentInsightsResponse getInsights(LocalDate asOfDate);
}
