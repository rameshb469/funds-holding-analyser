package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.model.ExtractorJobResponse;
import com.rms.funds.hodings.analyser.model.ExtractorJobUpdateRequest;
import com.rms.funds.hodings.analyser.repository.ExtractorJobRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundConfigRepository;
import com.rms.funds.hodings.analyser.service.ExtractorJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExtractorJobServiceImpl implements ExtractorJobService {

    private final ExtractorJobRepository extractorJobRepository;
    private final MutualFundConfigRepository mutualFundConfigRepository;

    @Override
    public List<ExtractorJobResponse> findAll() {
        return extractorJobRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (ExtractorJobEntity job) ->
                                Optional.ofNullable(job.getUpdatedAt()).orElse(job.getCreatedAt())
                ).reversed())
                .map(this::toDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ExtractorJobResponse> upsert(ExtractorJobUpdateRequest updateRequest) {
        if (updateRequest != null && !CollectionUtils.isEmpty(updateRequest.getUpdatedLinks())) {
            extractorJobRepository.saveAll(updateRequest.getUpdatedLinks().entrySet().stream()
                    .map(entry -> extractorJobRepository.findById(entry.getKey())
                            .map(entity -> entity.toBuilder().url(entry.getValue()).updatedAt(LocalDateTime.now()).build())
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList());
        }
        return findAll();
    }

    private ExtractorJobResponse toDTO(ExtractorJobEntity entity) {
        var mutualFund = mutualFundConfigRepository
                .findById(entity.getMutualFundConfigId())
                .map(MutualFundConfigEntity::getMutualFund)
                .orElse(null);
        if (mutualFund != null) {
            return ExtractorJobResponse.builder()
                    .id(entity.getId())
                    .mutualFundId(mutualFund.getId())
                    .mutualFundName(mutualFund.getName())
                    .url(entity.getUrl())
                    .atDate(entity.getAtDate())
                    .status(entity.getStatus())
                    .error(entity.getError())
                    .recordCount(entity.getRecordCount())
                    .build();
        }
        return null;
    }

}
