package com.rms.funds.hodings.analyser.loader;

import com.rms.funds.hodings.analyser.cache.StockInfoHolder;
import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.entity.MutualFundHoldingEntity;
import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.modal.Result;
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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.rms.funds.hodings.analyser.utility.DateUtil.getDownloadLinks;

@Component
@RequiredArgsConstructor
public class StockInfoLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StockInfoLoader.class);
    private final StockInfoRepository stockInfoRepository;
    private final FileDownloader fileDownloader;
    private final MutualFundConfigRepository mutualFundConfigRepository;
    private final MutualFundHoldingRepository holdingRepository;
    private final ExtractorJobRepository extractorJobRepository;
    private final StockInfoHolder stockInfoHolder;

    @Override
    public void run(String... args) throws Exception {

        Map<Long, List<MutualFundConfigEntity>> configEntitiesMap = mutualFundConfigRepository.findAll().stream()
                .filter(x -> x.getMutualFund() != null)
                .filter(MutualFundConfigEntity::getIsActive)
              //  .filter(x -> x.getMutualFund().getMutualFundHouseId().equals(1L))
                .collect(Collectors.groupingBy(MutualFundConfigEntity::getMutualFundId));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<Result>> futures = new ArrayList<>();
        for (var entry : configEntitiesMap.entrySet()){

            List<MutualFundConfigEntity> configList = entry.getValue();

            for (MutualFundConfigEntity config : configList) {
                List<Pair<String, LocalDate>> downloadLinks = getDownloadLinks(config);

                for (var downloadLinkPair : downloadLinks) {

                    if (holdingRepository.existsByMutualFundIdAndAtDate(config.getMutualFundId(),  downloadLinkPair.getRight())) {
                        log.info("Skipping Mutual fund job for {} mutual fund and {} date since its already existed", config.getMutualFundId(), downloadLinkPair.getRight());
                    }
                    futures.add(executorService.submit(new HoldingExtractor(fileDownloader, downloadLinkPair, config)));
                }
            }
        }

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

    private void saveMutualFundHoldings() {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<Result>> futures = new ArrayList<>();
        for (var entry : configEntitiesMap.entrySet()){

            List<MutualFundConfigEntity> configList = entry.getValue();

            for (MutualFundConfigEntity config : configList) {
                List<Pair<String, LocalDate>> downloadLinks = getDownloadLinks(config);

                for (var downloadLinkPair : downloadLinks) {

                    if (holdingRepository.existsByMutualFundIdAndAtDate(config.getMutualFundId(),  downloadLinkPair.getRight())) {
                        log.info("Skipping Mutual fund job for {} mutual fund and {} date since its already existed", config.getMutualFundId(), downloadLinkPair.getRight());
                    }
                    futures.add(executorService.submit(new HoldingExtractor(fileDownloader, downloadLinkPair, config)));
                }
            }
        }

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

            MutualFundConfigEntity config = mutualFundConfigRepository.findById(result.getConfigId()).orElseThrow(() -> new RuntimeException("Invalid Config Id : "+ result.getConfigId()));

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

              if (!CollectionUtils.isEmpty(mfHoldings)){
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

              extractorJobRepository.save(ExtractorJobEntity.builder()
                      .mutualFundConfigId(config.getId())
                      .atDate(result.getAtDate())
                      .url(result.getDownloadLink())
                      .status(status.name())
                      .error(result.getError())
                      .recordCount(holdings.size())
                      .build());
          } catch (Exception e) {
              log.error(e.getMessage());
          }

        }

//        for (String m : missingCodes) {
//            System.out.println(m);
//        }
    }

    private Double getFreeCashNetAssetPerc(MutualFundHoldingEntity entity, Double totalNetAssetPctValue) {
        if (totalNetAssetPctValue != null || totalNetAssetPctValue.equals(0.0)){
            return (entity.getMarketValue()/entity.getNetAssetPct())*totalNetAssetPctValue;
        }
        return 0.0;
    }
}
