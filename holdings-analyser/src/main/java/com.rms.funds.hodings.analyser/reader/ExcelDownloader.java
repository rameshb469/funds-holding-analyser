package com.rms.funds.hodings.analyser.reader;

import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.modal.SheetColumnMapper;

import java.io.File;
import java.util.List;

public interface ExcelDownloader {

    List<MutualFundStockHolding> load(String url, ExcelDownloaderAttributes attributes);

    List<MutualFundStockHolding> process(File file, String sheetName, SheetColumnMapper columnMapper);

    List<MutualFundStockHolding> process(File file, ExcelDownloaderAttributes attributes);

    }
