package com.rms.funds.hodings.analyser.schedulers;

// BhavcopyScheduler.java
import com.rms.funds.hodings.analyser.service.BhavcopyDownloaderService;
import com.rms.funds.hodings.analyser.service.BhavcopyParserService;
import com.rms.funds.hodings.analyser.service.BhavcopyService;
import com.rms.funds.hodings.analyser.service.impl.BseBhavcopyService;
import com.rms.funds.hodings.analyser.service.impl.NseBhavcopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BhavcopyScheduler implements CommandLineRunner {

    private final BhavcopyDownloaderService downloader;
    private final BhavcopyParserService parser;
    private final NseBhavcopyService nseBhavcopyService;

    // Run every weekday at 7:30 PM IST (Mon-Fri)
    @Scheduled(cron = "0 30 19 * * MON-FRI", zone = "Asia/Kolkata")
    public void fetchAndStoreBhavcopy() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            File csvFile = downloader.downloadBhavcopy(yesterday);
            parser.parseAndSave(csvFile);
            System.out.println("✅ Stored bhavcopy for " + yesterday);
        } catch (Exception e) {
            System.err.println("❌ Error in BhavcopyScheduler: " + e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        //fetchAndStoreBhavcopy();

        for (LocalDate date : getLast6MonthsWeekdays()) {
            System.out.println("Starting the date : "+date.format(DateTimeFormatter.ISO_DATE));
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                File csvFile = downloader.downloadBhavcopy(yesterday);
                parser.parseAndSave(csvFile);
                System.out.println("✅ Stored bhavcopy for " + yesterday);
            } catch (Exception e) {
                System.err.println("❌ Error in BhavcopyScheduler: " + e.getMessage()+ " for date "+date.format(DateTimeFormatter.ISO_DATE));
            }
            System.out.println("End the date : "+date.format(DateTimeFormatter.ISO_DATE));
        }
    }

    public List<LocalDate> getLast6MonthsWeekdays() {
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
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
