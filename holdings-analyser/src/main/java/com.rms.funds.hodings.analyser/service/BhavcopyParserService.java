package com.rms.funds.hodings.analyser.service;

import java.io.File;

public interface BhavcopyParserService {

    void parseAndSave(File csvFile) throws Exception;
}
