package com.rms.funds.holdings.analyser.reader;

import com.rms.funds.holdings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.holdings.analyser.model.MutualFundStockHolding;
import com.rms.funds.holdings.analyser.model.SheetColumnMapper;

import java.io.File;
import java.util.List;

public interface ExcelDownloader {

    List<MutualFundStockHolding> load(String url, ExcelDownloaderAttributes attributes);

    List<MutualFundStockHolding> process(File file, String sheetName, SheetColumnMapper columnMapper);

    List<MutualFundStockHolding> process(File file, ExcelDownloaderAttributes attributes);

    }
