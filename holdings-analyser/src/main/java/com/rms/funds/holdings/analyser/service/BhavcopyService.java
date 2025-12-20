package com.rms.funds.holdings.analyser.service;

import java.time.LocalDate;

public interface BhavcopyService {

    void fetchAndStoreBhavcopy(LocalDate date) throws Exception;
}