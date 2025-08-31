package com.rms.funds.hodings.analyser.helper;

import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.loader.HoldingInfoLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldingDownloaderHelper {

    private final HoldingInfoLoader holdingInfoLoader;

    public void retry(List<ExtractorJobEntity> extractorJobs) {
        if (!CollectionUtils.isEmpty(extractorJobs)) {
            log.info("Starting job from HoldingDownloaderHelper job");
            holdingInfoLoader.processJobs(extractorJobs);
        }
    }
}
