package com.rms.funds.hodings.analyser.modal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
public class MutualFundStockHolding {

    private String stockName;

    private String isinCode;

    private String industry;

    private Long quantity;

    private double marketValue;

    private double netAssetPerc;
}
