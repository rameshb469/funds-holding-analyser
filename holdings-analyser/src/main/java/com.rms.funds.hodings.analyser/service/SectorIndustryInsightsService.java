package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.SectorIndustryStockChangeDTO;

import java.time.LocalDate;
import java.util.List;

public interface SectorIndustryInsightsService {

    List<SectorIndustryStockChangeDTO> getInsights(LocalDate currentDate);
}
