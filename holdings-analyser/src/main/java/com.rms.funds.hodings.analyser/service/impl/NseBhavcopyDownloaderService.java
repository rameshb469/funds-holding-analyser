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

    private static final String BASE_URL = "https://www.nseindia.com/content/historical/EQUITIES";

    public File downloadBhavcopy(LocalDate date) throws IOException {
        String yyyy = String.valueOf(date.getYear());
        String mmm = date.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
        String ddmmmyyyy = date.format(DateTimeFormatter.ofPattern("ddMMMyyyy")).toUpperCase();

        String url = String.format("%s/%s/%s/cm%sbhav.csv.zip", BASE_URL, yyyy, mmm, ddmmmyyyy);

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

