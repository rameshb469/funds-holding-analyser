package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.controller.dto.MutualFundConfigDto;
import com.rms.funds.hodings.analyser.entity.*;
import com.rms.funds.hodings.analyser.repository.ExtractorJobRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundConfigRepository;
import com.rms.funds.hodings.analyser.service.MutualFundConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MutualFundConfigServiceImpl implements MutualFundConfigService {

    private final MutualFundConfigRepository mutualFundConfigRepository;
    private final ExtractorJobRepository extractorJobRepository;

    @Override
    public List<MutualFundConfigDto> findAll() {

        return mutualFundConfigRepository.findAll().stream()
                .map(this::getConfigDto)
                .toList();
    }

    @Override
    public MutualFundConfigDto create(MutualFundConfigDto dto) {
        return null;
    }

    private MutualFundConfigDto getConfigDto(MutualFundConfigEntity config) {
        Optional<MutualFundHouseEntity> houseEntity = Optional.ofNullable(config.getFundHouse());
        Optional<MutualFundTypeEntity> typeEntity = Optional.ofNullable(config.getFundType());

        List<ExtractorJobEntity> extractorJobEntities = extractorJobRepository.findByMutualFundConfigId(config.getMutualFundId());
        long successfulCount = 0, failedCount = 0;
        if (!CollectionUtils.isEmpty(extractorJobEntities)) {
            successfulCount = extractorJobRepository.findByMutualFundConfigId(config.getId())
                    .stream()
                    .filter(obj -> "SUCCESSFUL".equals(obj.getStatus()))
                    .count();
            failedCount = extractorJobEntities.size() - successfulCount;
        }

        return MutualFundConfigDto.builder()
                .id(config.getId())
                .mutualFundId(config.getMutualFundId())
                .mutualFundName(config.getMutualFund().getName())
                .mfHouseId(houseEntity.map(MutualFundHouseEntity::getId).orElse(null))
                .mfHouseName(houseEntity.map(MutualFundHouseEntity::getName).orElse(null))
                .mfTypeId(typeEntity.map(MutualFundTypeEntity::getId).orElse(null))
                .mfTypeName(typeEntity.map(MutualFundTypeEntity::getName).orElse(null))
                .configUrl(config.getDownloadUrl())
                .sheetName(config.getSheetName())
                .dateMapper1(config.getDateMapper1())
                .dateMapper2(config.getDateMapper2())
                .dateMapper3(config.getDateMapper3())
                .dateMapper4(config.getDateMapper4())
                .successfulCount(successfulCount)
                .failedCount(failedCount)
                .build();
    }
}
