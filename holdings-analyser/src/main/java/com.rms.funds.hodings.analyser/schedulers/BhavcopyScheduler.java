package com.rms.funds.hodings.analyser.schedulers;

// BhavcopyScheduler.java
import com.rms.funds.hodings.analyser.service.BhavcopyDownloaderService;
import com.rms.funds.hodings.analyser.service.BhavcopyParserService;
import com.rms.funds.hodings.analyser.service.impl.NseBhavcopyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BhavcopyScheduler implements CommandLineRunner {

    private final BhavcopyDownloaderService downloader;
    private final BhavcopyParserService parser;
    private final NseBhavcopyService nseBhavcopyService;

    // Run every weekday at 8:10 PM IST (Mon-Fri)
    @Scheduled(cron = "0 10 21 * * MON-FRI", zone = "Asia/Kolkata")
    public void fetchAndStoreBhavcopy() {
        try {
            LocalDate today = LocalDate.now();
            File csvFile = downloader.downloadBhavcopy(today);
            parser.parseAndSave(csvFile);
            log.info("✅ Stored bhavcopy for {}", today);
        } catch (Exception e) {
            log.error("❌ Error in BhavcopyScheduler: {}", e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {
            fetchAndStoreBhavcopy();

//        for (LocalDate date : getLast6MonthsWeekdays()) {
//            System.out.println("Starting the date : "+date.format(DateTimeFormatter.ISO_DATE));
//            try {
//              //  LocalDate yesterday = LocalDate.now().minusDays(1);
//                File csvFile = downloader.downloadBhavcopy(date);
//                parser.parseAndSave(csvFile);
//                System.out.println("✅ Stored bhavcopy for " + date);
//            } catch (Exception e) {
//                System.err.println("❌ Error in BhavcopyScheduler: " + e.getMessage()+ " for date "+date.format(DateTimeFormatter.ISO_DATE));
//            }
//            System.out.println("End the date : "+date.format(DateTimeFormatter.ISO_DATE));
//        }
    }

    public List<LocalDate> getLast6MonthsWeekdays() {
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusMonths(6);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdays.add(date);
            }
        }
        return weekdays;
    }
}
