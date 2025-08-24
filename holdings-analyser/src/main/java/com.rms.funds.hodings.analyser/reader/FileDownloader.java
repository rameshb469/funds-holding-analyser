package com.rms.funds.hodings.analyser.reader;

import com.rms.funds.hodings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.model.MutualFundStockHolding;

import java.net.URISyntaxException;
import java.util.List;

public interface FileDownloader {

    List<MutualFundStockHolding> downloadExcelFile(String url, ExcelDownloaderAttributes attributes) throws URISyntaxException;
}
