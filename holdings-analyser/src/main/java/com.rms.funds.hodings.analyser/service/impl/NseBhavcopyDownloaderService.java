package com.rms.funds.hodings.analyser.service.impl;

// BhavcopyDownloaderService.java
import com.rms.funds.hodings.analyser.service.BhavcopyDownloaderService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
        import java.io.*;
        import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

@Service
public class NseBhavcopyDownloaderService implements BhavcopyDownloaderService {

    private static final String BASE_URL = "https://nsearchives.nseindia.com/content/cm/";
            //"https://www.nseindia.com/content/historical/EQUITIES";

    //https://nsearchives.nseindia.com/content/cm/BhavCopy_NSE_CM_0_0_0_20251014_F_0000.csv.zip
    public File downloadBhavcopy(LocalDate date) throws IOException {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String url = String.format("%sBhavCopy_NSE_CM_0_0_0_%s_F_0000.csv.zip", BASE_URL, dateStr);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0"); // required by NSE
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to download: " + url);
        }

        byte[] zipBytes = response.getBody();
        File tempFile = File.createTempFile("bhavcopy_", ".csv");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry = zis.getNextEntry();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                zis.transferTo(fos);
            }
        }
        return tempFile;
    }
}
