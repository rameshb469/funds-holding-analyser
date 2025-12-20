package com.rms.funds.holdings.analyser.reader;

import com.rms.funds.holdings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.holdings.analyser.model.MutualFundStockHolding;

import java.net.URISyntaxException;
import java.util.List;

public interface FileDownloader {

    List<MutualFundStockHolding> downloadExcelFile(String url, ExcelDownloaderAttributes attributes) throws URISyntaxException;
}
