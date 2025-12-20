package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SectorIndustryStockChangeDTO {
    private Long sectorId;
    private String sectorName;

    private Long industryId;
    private String industryName;

    private Long stockId;
    private String stockName;
    private String stockSymbol;

    private Double currentValue;
    private Double prevValue;
    private Double changePct;
}
