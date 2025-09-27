package com.rms.funds.hodings.analyser.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public interface BhavcopyDownloaderService {

    File downloadBhavcopy(LocalDate date) throws IOException;

}
