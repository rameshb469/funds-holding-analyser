package com.rms.funds.holdings.analyser.service.impl;

import com.rms.funds.holdings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.holdings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.holdings.analyser.helper.HoldingDownloaderHelper;
import com.rms.funds.holdings.analyser.model.ExtractorJobResponse;
import com.rms.funds.holdings.analyser.model.ExtractorJobUpdateRequest;
import com.rms.funds.holdings.analyser.repository.ExtractorJobRepository;
import com.rms.funds.holdings.analyser.repository.MutualFundConfigRepository;
import com.rms.funds.holdings.analyser.service.ExtractorJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExtractorJobServiceImpl implements ExtractorJobService {

    private final ExtractorJobRepository extractorJobRepository;
    private final MutualFundConfigRepository mutualFundConfigRepository;
    private final HoldingDownloaderHelper holdingDownloaderHelper;

    @Override
    public List<ExtractorJobResponse> findAll() {
//        return extractorJobRepository.findAll().stream()
//                .sorted(Comparator.comparing(
//                        (ExtractorJobEntity job) ->
//                                Optional.ofNullable(job.getUpdatedAt()).orElse(job.getCreatedAt())
//                ).reversed())
//                .filter(distinctByKeys(ExtractorJobEntity::getMutualFundId, ExtractorJobEntity::getAtDate))
//                .map(this::toDTO)
//                .filter(Objects::nonNull)
//                .toList();

        // Fetch all, sort by updatedAt/createdAt desc, then dedupe by (mutualFundId, atDate) preferring SUCCESS status
        var list = extractorJobRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (ExtractorJobEntity job) ->
                                Optional.ofNullable(job.getUpdatedAt()).orElse(job.getCreatedAt())
                ).reversed())
                .collect(Collectors.toMap(
                        // key: pair of mutualFundId and atDate
                        job -> Arrays.asList(job.getMutualFundId(), job.getAtDate()),
                        Function.identity(),
                        (e1, e2) -> {
                            // merge function when duplicate key encountered: prefer SUCCESS status
                            String s1 = Optional.ofNullable(e1.getStatus()).orElse("");
                            String s2 = Optional.ofNullable(e2.getStatus()).orElse("");
                            if ("SUCCESS".equalsIgnoreCase(s1)) return e1;
                            if ("SUCCESS".equalsIgnoreCase(s2)) return e2;
                            // if neither is SUCCESS, keep the first encountered (e1) which respects sorted order
                            return e1;
                        },
                        LinkedHashMap::new
                ));

        return list.values().stream()
                .map(this::toDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        Set<List<?>> seen = ConcurrentHashMap.newKeySet();
        return t -> {
            List<?> keys = Arrays.stream(keyExtractors)
                    .map(k -> k.apply(t))
                    .toList();
            return seen.add(keys);
        };
    }


    @Override
    public List<ExtractorJobResponse> upsert(ExtractorJobUpdateRequest updateRequest) {
        if (updateRequest != null && !CollectionUtils.isEmpty(updateRequest.getUpdatedLinks())) {
            holdingDownloaderHelper.retry(extractorJobRepository.saveAll(updateRequest.getUpdatedLinks().entrySet().stream()
                    .map(entry -> extractorJobRepository.findById(entry.getKey())
                            .map(entity -> entity.toBuilder().url(entry.getValue()).updatedAt(LocalDateTime.now()).build())
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList()));
        }
        return updateRequest.getUpdatedLinks().keySet().stream()
                .map(s -> extractorJobRepository.findById(s).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .toList();
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
