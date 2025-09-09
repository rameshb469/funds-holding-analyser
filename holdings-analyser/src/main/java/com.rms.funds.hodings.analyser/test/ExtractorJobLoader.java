package com.rms.funds.hodings.analyser.test;

import com.rms.funds.hodings.analyser.cache.StockInfoHolder;
import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundHoldingEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.loader.HoldingInfoLoader;
import com.rms.funds.hodings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.model.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.model.Result;
import com.rms.funds.hodings.analyser.reader.FileDownloader;
import com.rms.funds.hodings.analyser.repository.*;
import com.rms.funds.hodings.analyser.utility.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.rms.funds.hodings.analyser.helper.MapperUtil.getAttributes;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractorJobLoader
//implements CommandLineRunner
{

    private final MutualFundConfigRepository configRepository;
    private final ExtractorJobRepository extractorJobRepository;
    private final MutualFundHoldingRepository holdingRepository;
    private final FileDownloader fileDownloader;
    private final StockInfoHolder stockInfoHolder;

    //@Override
    public void run(String... args) throws Exception {

        List<MutualFundConfigEntity> configEntities = configRepository.findAll()
                .stream()
                .filter(mf -> mf.getMutualFundId() >= 79 && mf.getMutualFundId() <= 88)
                .toList();

        for (MutualFundConfigEntity config : configEntities) {
            List<Pair<String, LocalDate>> links = DateUtil.getDownloadLinks(config).stream().limit(2).toList();

            int index = 0;
            List<Result> results = new ArrayList<>();
            for (var pair : links){

              //  if (index++ == 0) continue;

                if (!extractorJobRepository.exists(Example.of(ExtractorJobEntity.builder()
                                .mutualFundConfigId(config.getId())
                                .atDate(pair.getRight())
                        .build()))) {
                    System.out.println("Need to add At date : "+pair.getRight()+" link : "+pair.getLeft() +" for config "+config.getMutualFund().getName());
                    var jobEntry = extractorJobRepository.save(ExtractorJobEntity.builder()
                            .url(pair.getLeft())
                            .atDate(pair.getRight())
                            .mutualFundConfigId(config.getId())
                            .config(config)
                            .recordCount(0)
                            .recordCount(0)
                            .recordCount(0)
                            .status("FAILED")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());

                    Result result = call(pair, config);
                    results.add(result);

                } {
                    System.out.println("Already there At date : "+pair.getRight()+" link : "+pair.getLeft() +" for config "+config.getMutualFund().getName());
                }
            }
            saveMutualFundHoldings(results);
        }

    }

    public void save(List<Pair<String, LocalDate>> pairs, MutualFundConfigEntity config) {

        List<Result> results = new ArrayList<>();

        for (var pair : pairs) {
            try {
                Result result = call(pair, config);
                results.add(result);
            } catch (Exception e) {
                log.error("error occurred {} and date : {}", pair.getLeft(), pair.getRight());
            }
        }

        saveMutualFundHoldings(results);
    }

    private Result call(Pair<String, LocalDate> downloadLinkPair,
                       MutualFundConfigEntity config) throws Exception {
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


    private void saveMutualFundHoldings(List<Result> results) {
        // Set<String> missingCodes = new HashSet<>();
        for (Result result : results) {
            List<MutualFundStockHolding> holdings = (result.getHoldings() != null) ? result.getHoldings() : new ArrayList<>();

//            holdings.forEach(h -> {
//                        var info = stockInfoHolder.getByIsinCode(h.getIsinCode()).orElse(null);
//                        if (info == null){
//                            missingCodes.add(h.getIsinCode());
//                        }
//                    });
//
//            if (true) {
//                continue;
//            }

            Result.Status status = result.getStatus();

            MutualFundConfigEntity config = configRepository.findById(result.getConfigId()).orElseThrow(() -> new RuntimeException("Invalid Config Id : " + result.getConfigId()));

            try {
                AtomicReference<Double> totalNetAssetPct = new AtomicReference<>(100.00);
                var mfHoldings = new ArrayList<>(holdings.stream()
                        .peek(holding -> totalNetAssetPct.updateAndGet(v -> v - holding.getNetAssetPerc()))
                        .map(holding -> MutualFundHoldingEntity.builder()
                                .mutualFundId(config.getMutualFundId())
                                .stockId(stockInfoHolder.getByIsinCode(holding.getIsinCode()).map(StockInfoEntity::getId)
                                        .orElseThrow(() -> new RuntimeException("Invalid ISIN Code : " + holding.getIsinCode())))
                                .quantity(holding.getQuantity())
                                .marketValue(holding.getMarketValue())
                                .netAssetPct(holding.getNetAssetPerc())
                                .atDate(result.getAtDate())
                                .build())
                        .toList());

                if (!CollectionUtils.isEmpty(mfHoldings)) {
                    Double totalNetAssetPctValue = totalNetAssetPct.get();
                    mfHoldings.add(MutualFundHoldingEntity.builder()
                            .mutualFundId(config.getMutualFundId())
                            .stockId(stockInfoHolder.getFreeCashStockInfo().map(StockInfoEntity::getId)
                                    .orElseThrow(() -> new RuntimeException("Unable to find free cash code")))
                            .quantity(0L)
                            .marketValue(getFreeCashNetAssetPerc(mfHoldings.get(0), totalNetAssetPctValue))
                            .netAssetPct(totalNetAssetPctValue)
                            .atDate(result.getAtDate())
                            .build());

                    holdingRepository.saveAll(mfHoldings);
                }

                saveExtraJobInfo(config, holdings.size(), result);
            } catch (Exception e) {
                log.error("Error: ---> "+e.getMessage());
                saveExtraJobInfo(config, 0, result.toBuilder()
                        .status(Result.Status.FAILED)
                        .error(e.getMessage()).build());
            }

        }

//        for (String m : missingCodes) {
//            System.out.println(m);
//        }
    }

    private void saveExtraJobInfo(MutualFundConfigEntity config,
                                  int recordCount,
                                  Result result) {
        var existingJobRecord = extractorJobRepository.findOne(Example.of(ExtractorJobEntity.builder()
                .mutualFundConfigId(config.getId())
                .url(result.getDownloadLink())
                .build()));
        if (existingJobRecord.isPresent()) {
            extractorJobRepository.save(existingJobRecord.get().toBuilder()
                    .url(result.getDownloadLink())
                    .error(result.getError())
                    .recordCount(recordCount)
                    .updatedAt(LocalDateTime.now())
                    .build());

        } else {
            extractorJobRepository.save(ExtractorJobEntity.builder()
                    .mutualFundConfigId(config.getId())
                    .atDate(result.getAtDate())
                    .url(result.getDownloadLink())
                    .status(result.getStatus().name())
                    .error(result.getError())
                    .recordCount(recordCount)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
    }

    private Double getFreeCashNetAssetPerc(MutualFundHoldingEntity entity, Double totalNetAssetPctValue) {
        if (totalNetAssetPctValue != null || totalNetAssetPctValue.equals(0.0)) {
            return (entity.getMarketValue() / entity.getNetAssetPct()) * totalNetAssetPctValue;
        }
        return 0.0;
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
