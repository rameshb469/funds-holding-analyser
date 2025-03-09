package com.rms.funds.hodings.analyser.reader;

import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileDownloaderImpl implements FileDownloader{

    private final RestTemplate restTemplate;

    private final ExcelDownloader excelDownloader;

    @Override
    public List<MutualFundStockHolding> downloadExcelFile(final String url,
                                                          final ExcelDownloaderAttributes attributes) {
        String fileName = attributes.getMutualFundName()+"-rest-"+System.currentTimeMillis();
      //  URI uri = new URI(url);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-disposition", "form-data; name=file; filename="+fileName+".xlsx");
        headers.put("Content-type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //headers.put("Origin", uri.getHost());
        File file = restTemplate.execute(url, HttpMethod.GET, clientHttpRequest ->
                clientHttpRequest.getHeaders().setAll(headers),
                clientHttpResponse -> {
            File ret = File.createTempFile("download", "tmp");
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
        return excelDownloader.process(file, attributes);
    }
}
