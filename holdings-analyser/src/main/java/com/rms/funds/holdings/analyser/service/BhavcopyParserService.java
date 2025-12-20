package com.rms.funds.holdings.analyser.service;

import java.io.File;

public interface BhavcopyParserService {

    void parseAndSave(File csvFile) throws Exception;
}
