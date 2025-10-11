package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.entity.StockInfoEntity;

import java.util.Optional;

public interface StockDataFetchService {
    Optional<StockInfoEntity> findByIsinNumber(String isinCsv);
}
