package com.rms.funds.hodings.analyser.helper;

import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.loader.HoldingInfoLoader;
import com.rms.funds.hodings.analyser.test.ExtractorJobLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldingDownloaderHelper {

    private final HoldingInfoLoader holdingInfoLoader;
    private final ExtractorJobLoader extractorJobLoader;

    public void retry(List<ExtractorJobEntity> extractorJobs) {
        if (!CollectionUtils.isEmpty(extractorJobs)) {
            log.info("Starting job from HoldingDownloaderHelper job");
           // holdingInfoLoader.processJobs(extractorJobs);

            var groupByConfigId = extractorJobs.stream()
                    .collect(groupingBy(ExtractorJobEntity::getMutualFundConfigId));

            for (var entry : groupByConfigId.entrySet()) {
                Optional.ofNullable(entry.getValue().get(0))
                        .map(ExtractorJobEntity::getConfig)
                        .ifPresent(config -> extractorJobLoader.save(entry.getValue().stream().map(e -> Pair.of(e.getUrl(), e.getAtDate())).toList(), config));
            }


        }
    }
}
