package com.rms.funds.hodings.analyser.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockMovementResult {
    private Long stockId;
    private String symbol;
    private String isin;
    private LocalDate tradeDate;
    private LocalDate previousDate;
    private BigDecimal previousClose;
    private BigDecimal close;
    private BigDecimal absoluteChange;
    private Double percentChange;

    // CSV comparison
    private BigDecimal csvClose;
    private boolean csvMatches;
    private String note;

    public StockMovementResult() {}

    // getters/setters
    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public LocalDate getPreviousDate() { return previousDate; }
    public void setPreviousDate(LocalDate previousDate) { this.previousDate = previousDate; }
    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    public BigDecimal getAbsoluteChange() { return absoluteChange; }
    public void setAbsoluteChange(BigDecimal absoluteChange) { this.absoluteChange = absoluteChange; }
    public Double getPercentChange() { return percentChange; }
    public void setPercentChange(Double percentChange) { this.percentChange = percentChange; }
    public BigDecimal getCsvClose() { return csvClose; }
    public void setCsvClose(BigDecimal csvClose) { this.csvClose = csvClose; }
    public boolean isCsvMatches() { return csvMatches; }
    public void setCsvMatches(boolean csvMatches) { this.csvMatches = csvMatches; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

