package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.controller.dto.SectorIndustryStockChangeDTO;
import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import com.rms.funds.hodings.analyser.repository.MfHoldingRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundHoldingRepository;
import com.rms.funds.hodings.analyser.service.SectorIndustryInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectorIndustryInsightsServiceImpl implements SectorIndustryInsightsService {

    private final MfHoldingRepository mfHoldingRepository;

    @Override
    public List<SectorIndustryStockChangeDTO> getInsights(LocalDate currentDate) {

        if (currentDate == null) {
            currentDate = mfHoldingRepository.findAll().stream().max(Comparator.comparing(MfHoldingEntity::getAtDate))
                    .map(MfHoldingEntity::getAtDate)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to fetch last date reports"));
        }
        LocalDate prevDate = currentDate.minusMonths(1).withDayOfMonth(currentDate.minusMonths(1).lengthOfMonth());
        return mfHoldingRepository.findSectorIndustryChanges(currentDate, prevDate);
    }
}

