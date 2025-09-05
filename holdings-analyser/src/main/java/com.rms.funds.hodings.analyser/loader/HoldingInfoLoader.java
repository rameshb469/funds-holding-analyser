package com.rms.funds.hodings.analyser.loader;

import com.rms.funds.hodings.analyser.cache.StockInfoHolder;
import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundHoldingEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.model.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.model.Result;
import com.rms.funds.hodings.analyser.reader.FileDownloader;
import com.rms.funds.hodings.analyser.repository.ExtractorJobRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundConfigRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundHoldingRepository;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static com.rms.funds.hodings.analyser.utility.DateUtil.getDownloadLinks;
import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
public class HoldingInfoLoader {

    private static final Logger log = LoggerFactory.getLogger(HoldingInfoLoader.class);
    private final StockInfoRepository stockInfoRepository;
    private final FileDownloader fileDownloader;
    private final MutualFundConfigRepository mutualFundConfigRepository;
    private final MutualFundHoldingRepository holdingRepository;
    private final ExtractorJobRepository extractorJobRepository;
    private final StockInfoHolder stockInfoHolder;

    public void processJobs(List<ExtractorJobEntity> extractorJobEntities) {
        if (!CollectionUtils.isEmpty(extractorJobEntities)) {
            var groupByConfigId = extractorJobEntities.stream()
                    .collect(groupingBy(ExtractorJobEntity::getMutualFundConfigId));

            for (var entry : groupByConfigId.entrySet()) {
                Optional.ofNullable(entry.getValue().get(0))
                        .map(ExtractorJobEntity::getConfig)
                        .ifPresent(config -> startJob(config, entry.getValue().stream().map(e -> Pair.of(e.getUrl(), e.getAtDate())).toList()));
            }
        }
    }

    private void startJob(MutualFundConfigEntity config,
                          List<Pair<String, LocalDate>> downloadLinks){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Future<Result>> futures = new ArrayList<>();
        log.info("starting job config : {} ", config.getId());

        for (var downloadLinkPair : downloadLinks) {

            if (holdingRepository.existsByMutualFundIdAndAtDate(config.getMutualFundId(), downloadLinkPair.getRight())) {
                log.info("Skipping Mutual fund job for {} mutual fund and {} date since its already existed", config.getMutualFundId(), downloadLinkPair.getRight());
                return;
            }
            futures.add(executorService.submit(new HoldingExtractor(fileDownloader, downloadLinkPair, config)));

            // Collecting results
            List<Result> results = new ArrayList<>();
            for (Future<Result> future : futures) {
                try {
                    results.add(future.get()); // Wait for task to complete
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Unable to process it.. ");
                }
            }

            // Shutdown executor
            executorService.shutdown();
            saveMutualFundHoldings(results);
        }
    }

    public void process(List<MutualFundConfigEntity> configList) {

        for (MutualFundConfigEntity config : configList) {
            List<Pair<String, LocalDate>> downloadLinks = getDownloadLinks(config);
            startJob(config, downloadLinks);
        }
    }

    public void processAll() throws Exception {

        Map<Long, List<MutualFundConfigEntity>> configEntitiesMap = mutualFundConfigRepository.findAll().stream()
                .filter(x -> x.getMutualFund() != null)
                .filter(MutualFundConfigEntity::getIsActive)
                .filter(x -> x.getMutualFund().getMutualFundHouseId().equals(1L))
                .collect(groupingBy(MutualFundConfigEntity::getMutualFundId));

        for (var entry : configEntitiesMap.entrySet()) {
            process(entry.getValue());
        }
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

            MutualFundConfigEntity config = mutualFundConfigRepository.findById(result.getConfigId()).orElseThrow(() -> new RuntimeException("Invalid Config Id : " + result.getConfigId()));

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
}
