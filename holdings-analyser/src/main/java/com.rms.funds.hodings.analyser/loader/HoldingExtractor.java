package com.rms.funds.hodings.analyser.loader;

import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.model.Result;
import com.rms.funds.hodings.analyser.reader.FileDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.concurrent.Callable;

import static com.rms.funds.hodings.analyser.helper.MapperUtil.getAttributes;

@RequiredArgsConstructor
@Slf4j
public class HoldingExtractor implements Callable<Result> {

    private final FileDownloader fileDownloader;
    private final Pair<String, LocalDate> downloadLinkPair;
    private final MutualFundConfigEntity config;

    @Override
    public Result call() throws Exception {
        String link = downloadLinkPair.getLeft();

        LocalDate atDate = downloadLinkPair.getRight();

        Result result = new Result();
        result.setConfigId(config.getId());
      //  result.setMutualFundId(mutualFundId);
        result.setName(getFundName(config));
        result.setAtDate(atDate);
        result.setDownloadLink(link);
        ExcelDownloaderAttributes attributes = getAttributes(link, config);

        try {
            var holdings =  fileDownloader.downloadExcelFile(attributes.getUrl(), attributes);
            result.setHoldings(holdings);
            result.setStatus(Result.Status.SUCCESSFUL);

        } catch (Exception e) {
            log.error("{} Error occurred while called rest template {}", attributes.getUrl(), e.getLocalizedMessage());
            result.setError(e.getMessage());
            result.setStatus(Result.Status.FAILED);
        }

        return result;
    }

    private String getFundName(MutualFundConfigEntity config) {
        if (config != null && config.getMutualFund() != null) {
            return config.getMutualFund().getName();
        } else if (config != null) {
            return "Invalid -mutual-fund-id-config-id"+config.getId();
        }
        return "Invalid -mutual-fund-id-config-id";
    }
}
