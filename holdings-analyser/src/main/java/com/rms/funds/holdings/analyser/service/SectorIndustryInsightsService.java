package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.SectorIndustryStockChangeDTO;

import java.time.LocalDate;
import java.util.List;

public interface SectorIndustryInsightsService {

    List<SectorIndustryStockChangeDTO> getInsights(LocalDate currentDate);
}
