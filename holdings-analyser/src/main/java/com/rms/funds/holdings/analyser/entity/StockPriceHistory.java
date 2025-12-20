package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "trade_date"}))
@Data
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private StockInfoEntity stock;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    private Long volume;
    private BigDecimal turnover;
    private Long numberOfTrades;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // Bhav Copy columns
    private LocalDate bizDate; // BizDt
    private String segment; // Sgmt
    private String source; // Src
    private String financialInstrumentType; // FinInstrmTp
    private String financialInstrumentId; // FinInstrmId
    private String isin; // ISIN
    private String tickerSymbol; // TckrSymb
    private String securitySeries; // SctySrs
    private LocalDate expiryDate; // XpryDt
    private LocalDate actualExpiryDate; // FininstrmActlXpryDt
    private BigDecimal strikePrice; // StrkPric
    private String optionType; // OptnTp
    private String instrumentName; // FinInstrmNm
    private BigDecimal lastPrice; // LastPric
    private BigDecimal previousClosingPrice; // PrvsClsgPric
    private BigDecimal underlyingPrice; // UndrlygPric
    private BigDecimal settlementPrice; // SttlmPric
    private Long openInterest; // OpnIntrst
    private Long changeInOpenInterest; // ChngInOpnIntrst
    private Long totalTradingVolume; // TtlTradgVol
    private BigDecimal totalTradedValue; // TtlTrfVal
    private Long totalNumberOfTransactionsExecuted; // TtlNbOfTxsExctd
    private String sessionId; // SsnId
    private Long newBoardLotQuantity; // NewBrdLotQty
    private String remarks; // Rmks
    private String reserved1; // Rsvd1
    private String reserved2; // Rsvd2
    private String reserved3; // Rsvd3
    private String reserved4; // Rsvd4

    // getters/setters
}
