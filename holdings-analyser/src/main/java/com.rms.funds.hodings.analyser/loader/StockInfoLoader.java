package com.rms.funds.hodings.analyser.loader;

import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.modal.Result;
import com.rms.funds.hodings.analyser.reader.FileDownloader;
import com.rms.funds.hodings.analyser.repository.MutualFundConfigRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundHoldingRepository;
import com.rms.funds.hodings.analyser.repository.StockInfoRepository;
import com.rms.funds.hodings.analyser.test.TestApp;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    @Override
    public void run(String... args) throws Exception {

        Map<Long, List<MutualFundConfigEntity>> configEntitiesMap = mutualFundConfigRepository.findAll().stream()
                .filter(x -> x.getMutualFund() != null)
                .filter(MutualFundConfigEntity::getIsActive)
                .collect(Collectors.groupingBy(MutualFundConfigEntity::getMutualFundId));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<Result>> futures = new ArrayList<>();
        for (var entry : configEntitiesMap.entrySet()){

            Long mutualFundId = entry.getKey();
            List<MutualFundConfigEntity> configList = entry.getValue();

            for (MutualFundConfigEntity config : configList) {
                List<Pair<String, LocalDate>> downloadLinks = getDownloadLinks(config);

                for (var downloadLinkPair : downloadLinks) {

                    if (holdingRepository.isExistedBymMutualFundIdAndAtDate(config.getMutualFundId(),  downloadLinkPair.getRight())) {
                        log.info("Skipping Mutual fund job for {} mutual fund and {} date since its already existed", config.getMutualFundId(), downloadLinkPair.getRight());
                    }
                    futures.add(executorService.submit(new HoldingExtractor(fileDownloader, downloadLinkPair, config, mutualFundId)));
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


    }
}
