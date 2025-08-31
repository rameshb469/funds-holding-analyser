package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.MutualFundConfigDto;

import java.util.List;

public interface MutualFundConfigService {

    List<MutualFundConfigDto> findAll();

    MutualFundConfigDto create(MutualFundConfigDto dto);
}
