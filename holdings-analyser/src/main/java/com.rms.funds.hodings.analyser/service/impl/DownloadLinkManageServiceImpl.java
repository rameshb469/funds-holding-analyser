package com.rms.funds.hodings.analyser.service.impl;

import com.rms.funds.hodings.analyser.controller.dto.DownloadLinkCreateRequest;
import com.rms.funds.hodings.analyser.entity.MutualFundDownloadLinkEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundEntity;
import com.rms.funds.hodings.analyser.repository.MutualFundDownloadLinkRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundRepository;
import com.rms.funds.hodings.analyser.service.DownloadLinkManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DownloadLinkManageServiceImpl implements DownloadLinkManageService {

    private final MutualFundRepository mutualFundRepository;
    private final MutualFundDownloadLinkRepository linkRepository;

    @Override
    public DownloadLinkCreateRequest upsert(DownloadLinkCreateRequest request) {

        if (request != null && request.getDownloadLinks() != null) {

            List<MutualFundDownloadLinkEntity> downloadLinkEntities = new ArrayList<>();
            for (var entry : request.getDownloadLinks().entrySet()) {
                Long mutualFundId = entry.getKey();
                MutualFundEntity mutualFund = mutualFundRepository.findById(mutualFundId)
                        .orElseThrow(() -> new RuntimeException("Invalid mutual fund identifier : "+mutualFundId));

                var downloadUrls = entry.getValue();
                if (!CollectionUtils.isEmpty(downloadUrls)) {
                    for (Map.Entry<String, String> dateEntry : downloadUrls.entrySet()){
                        LocalDate atDate = LocalDate.parse(dateEntry.getKey());
                        downloadLinkEntities.add(upsertDownloadLinks(mutualFund, atDate, dateEntry.getValue()));
                    }
                }
            }

            if (!CollectionUtils.isEmpty(downloadLinkEntities)) {
                linkRepository.saveAll(downloadLinkEntities);
            }
        }

        return request;
    }

    @Override
    public DownloadLinkCreateRequest getAll() {
        Map<Long, Map<String, String>> downloadLinks = new LinkedHashMap<>();
        for (var link : linkRepository.findAll()) {
            downloadLinks.computeIfAbsent(link.getMutualFundId(),
                    value -> new LinkedHashMap<>())
                    .put(link.getAtDate().format(DateTimeFormatter.ISO_DATE), link.getUrl());
        }
        return DownloadLinkCreateRequest.builder()
                .downloadLinks(downloadLinks)
                .build();
    }

    private MutualFundDownloadLinkEntity upsertDownloadLinks(MutualFundEntity mutualFund,
                                                             LocalDate atDate,
                                                             String newUrl) {
        var linkEntity = linkRepository.findByMutualFundIdAndAtDate(mutualFund.getId(), atDate)
                .map(link -> link.toBuilder().url(newUrl).updatedAt(LocalDateTime.now()).build())
                .orElse(null);
        if (linkEntity == null) {
            linkEntity = MutualFundDownloadLinkEntity.builder()
                    .mutualFundId(mutualFund.getId())
                    .atDate(atDate)
                    .url(newUrl)
                    .build();
        }
        return linkEntity;
    }
}
