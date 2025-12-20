package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.entity.StockInfoEntity;

import java.util.Optional;

public interface StockDataFetchService {
    Optional<StockInfoEntity> findByIsinNumber(String isinCsv);
}
