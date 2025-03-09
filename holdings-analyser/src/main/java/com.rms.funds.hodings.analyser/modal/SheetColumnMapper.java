package com.rms.funds.hodings.analyser.modal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
public class SheetColumnMapper {

    private Integer isin;
    private Integer stockName;
    private Integer industry;
    private Integer quantity;
    private Integer marketValue;

    private Integer netAssetPerc;

    public boolean isNull(){
        return (isin == null && stockName == null && quantity == null && marketValue == null);
    }
}
