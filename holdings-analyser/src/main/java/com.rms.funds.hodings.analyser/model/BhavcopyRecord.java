package com.rms.funds.hodings.analyser.model;

import lombok.Data;

@Data
public class BhavcopyRecord {
    private String symbol;
    private String series;
    private String date;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;              // Traded quantity (volume)
    private String numberOfTrades;      // Number of trades for the scrip
    private String valueOfSharesTraded; // Value of shares traded (turnover)
    // Add other fields as required
}
