package com.rms.funds.holdings.analyser.service.impl;

import java.time.LocalDate;

public class StockHistoryValidationResult {
    private Long stockId;
    private String symbol;
    private String isin;
    private boolean hasPrevious;
    private LocalDate previousTradeDate;
    private String note;

    public StockHistoryValidationResult() {}

    public StockHistoryValidationResult(Long stockId, String symbol, String isin, boolean hasPrevious, LocalDate previousTradeDate, String note) {
        this.stockId = stockId;
        this.symbol = symbol;
        this.isin = isin;
        this.hasPrevious = hasPrevious;
        this.previousTradeDate = previousTradeDate;
        this.note = note;
    }

    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    public LocalDate getPreviousTradeDate() { return previousTradeDate; }
    public void setPreviousTradeDate(LocalDate previousTradeDate) { this.previousTradeDate = previousTradeDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

