package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.MutualFundConfigDto;

import java.util.List;

public interface MutualFundConfigService {

    List<MutualFundConfigDto> findAll();

    MutualFundConfigDto create(MutualFundConfigDto dto);
}
