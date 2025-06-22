package com.rms.funds.hodings.analyser.reader;

import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rms.funds.hodings.analyser.utility.RetryHelperUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDownloaderImpl implements FileDownloader{

    private final RestTemplate nipponMFApiRestTemplate;
    private final RestTemplate restTemplate;

    private final ExcelDownloader excelDownloader;

    private RestTemplate getRestTemplate(final ExcelDownloaderAttributes attributes) {
        if (NIPPON.equalsIgnoreCase(attributes.getMutualFundHouse())) {
            return nipponMFApiRestTemplate;
        }
        return restTemplate;
    }

    @Override
    public List<MutualFundStockHolding> downloadExcelFile(final String url,
                                                          final ExcelDownloaderAttributes attributes) {

        int retryCount = isRetryRequired(attributes) ? 1 : 0;

        return downloadingFiles(url, attributes, retryCount, null);
    }

    private List<MutualFundStockHolding> downloadingFiles(final String url,
                                                          final ExcelDownloaderAttributes attributes,
                                                          int retryCount,
                                                          Exception e) {
        if (retryCount < 0) {
            throw new RuntimeException(e);
        }

        try {
            String fileName = attributes.getMutualFundName()+"-rest-"+System.currentTimeMillis();
            //URI uri = new URI(url);
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-disposition", "form-data; name=file; filename="+fileName+".xlsx");
            headers.put("Content-type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            headers.put("User-Agent",  "PostmanRuntime/7.37.3");
           // headers.put("Host", uri.getHost());
            File file = getRestTemplate(attributes).execute(url, HttpMethod.GET, clientHttpRequest ->
                            clientHttpRequest.getHeaders().setAll(headers),
                    clientHttpResponse -> {
                        File ret = File.createTempFile("download", "tmp");
                        StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
                        return ret;
                    });
            return excelDownloader.process(file, attributes);
        } catch (Exception exception) {
            log.error("retry count ; {} and error ", retryCount, exception);
            Pair<String, ExcelDownloaderAttributes> updateAttributes = updateAttributes(url, attributes);
            return downloadingFiles(updateAttributes.getLeft(), updateAttributes.getRight(), retryCount-1, exception);
        }
    }
}
