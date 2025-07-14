package com.rms.funds.hodings.analyser.test;

import com.opencsv.CSVWriter;
import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.modal.SheetColumnMapper;
import com.rms.funds.hodings.analyser.reader.FileDownloader;
import com.rms.funds.hodings.analyser.repository.ExtractorJobRepository;
import com.rms.funds.hodings.analyser.repository.MutualFundConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.rms.funds.hodings.analyser.utility.DateUtil.getDownloadLinks;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestApp implements CommandLineRunner {

    private final MutualFundConfigRepository mutualFundConfigRepository;
    private final ExtractorJobRepository extractorJobRepository;

    private final FileDownloader fileDownloader;


    @Override
    public void run(String... args) throws Exception {
        Map<Long, List<MutualFundConfigEntity>> configEntitiesMap = mutualFundConfigRepository.findAll().stream()
                .filter(x -> x.getMutualFund() != null)
                .filter(MutualFundConfigEntity::getIsActive)
         //       .filter(x -> x.getId().equals(73L))
          //      .filter(x -> x.getMutualFundId().equals(140L))
                .filter(x -> x.getMutualFund().getMutualFundHouseId().equals(7L))
                .collect(Collectors.groupingBy(MutualFundConfigEntity::getMutualFundId));

       // configEntities = Arrays.asList(configEntities.get(0));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<Result>> futures = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        for (var entry : configEntitiesMap.entrySet()){

            Long mutualFundId = entry.getKey();
            List<MutualFundConfigEntity> configList = entry.getValue();

            for (MutualFundConfigEntity config : configList) {
                List<Pair<String, LocalDate>> downloadLinks = getDownloadLinks(config);

                for (var downloadLinkPair : downloadLinks) {
                    futures.add(executorService.submit(new MyDataFetcher(downloadLinkPair, config, mutualFundId)));
//                var fetch = new MyDataFetcher(downloadLinkPair, config);
//                results.add(fetch.call());
                }
            }
        }

        // Collecting results
        for (Future<Result> future : futures) {
            try {
                results.add(future.get()); // Wait for task to complete
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown executor
        executorService.shutdown();

        generateResults(results);

    }

    class MyDataFetcher implements Callable<Result> {
        private final Pair<String, LocalDate> downloadLinkPair;
        private final MutualFundConfigEntity config;

        private final Long mutualFundId;

        public MyDataFetcher(Pair<String, LocalDate> downloadLinkPair, MutualFundConfigEntity config, Long mutualFundId) {
            this.downloadLinkPair = downloadLinkPair;
            this.config = config;
            this.mutualFundId = mutualFundId;
        }

        @Override
        public Result call() throws IOException {
            String link = downloadLinkPair.getLeft();
//            if (config.getMutualFund() != null && config.getMutualFund().getMutualFundHouseId().equals(18)) {
//                link = downloadLinkPair.getLeft().toLowerCase();
//            }

            LocalDate atDate = downloadLinkPair.getRight();

            Result result = new Result();
            result.setName(getName(config));
            result.setAtDate(atDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            result.setDownloadLink(link);

            try {
                var list = extractFileUsingRest(getAttributes(link, config));
                result.setCount(list.size());
                result.setStatus("OK");

            } catch (Exception e) {
                result.setError(e.getMessage());
                result.setStatus("ERROR");
            }

            return result;
        }

        private String getName(MutualFundConfigEntity config) {
            if (config != null && config.getMutualFund() != null) {
                return config.getMutualFund().getName();
            } else if (config != null) {
                return "Invalid -mutual-fund-id-config-id"+config.getId();
            }
            return "Invalid -mutual-fund-id-config-id";
        }
    }

    public static void generateResults(List<Result> results) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter("Results.csv"),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        writer.writeAll(toStringArray(results));

        writer.close();

        System.out.println("Completed.........");
    }

    private static List<String[]> toStringArray(List<Result> emps) {
        List<String[]> records = new ArrayList<String[]>();

        // adding header record
        records.add(new String[] { "Name", "DownloadLink", "Date", "Count", "Error", "Results" });

        Iterator<Result> it = emps.iterator();
        while (it.hasNext()) {
            Result emp = it.next();
            records.add(new String[] { emp.getName(), emp.getDownloadLink(), emp.getAtDate(), emp.getCount()+"",  emp.getError(), emp.getStatus()});
        }
        return records;
    }

    private ExcelDownloaderAttributes getAttributes(String link, final MutualFundConfigEntity config){

        return ExcelDownloaderAttributes.builder()
                .url(link)
                .mutualFundName(config.getMutualFund().getName())
                .mutualFundHouse(config.getMutualFund().getHouseEntity().getName())
                .fundTypeName(config.getMutualFund().getTypeEntity().getName())
                .sheetName(config.getSheetName())
                .contentType(config.getContentType())
                .extension(config.getExtension())
                .isPickupBySystem(config.isPickValuesBySystem())
                .sheetColumnMapper(SheetColumnMapper.builder()
                        .isin(config.getIsinCodeColNumber())
                        .stockName(config.getStockNameColNumber())
                        .industry(config.getIndustryCodeColumnNumber())
                        .quantity(config.getQuantityColNumber())
                        .netAssetPerc(config.getNetAssetPercColNumber())
                        .marketValue(config.getMarketValueColNumber())
                        .build())
                .build();
    }

    private List<MutualFundStockHolding> extractFileUsingRest(ExcelDownloaderAttributes attributes) throws URISyntaxException {
        try {
            return fileDownloader.downloadExcelFile(attributes.getUrl(), attributes);
        } catch (Exception e) {
            log.error(attributes.getUrl()+" Error occurred while called rest tempalte"+e.getLocalizedMessage());
           // e.printStackTrace();
            throw e;
           // return Collections.emptyList();
        }
    }
}
