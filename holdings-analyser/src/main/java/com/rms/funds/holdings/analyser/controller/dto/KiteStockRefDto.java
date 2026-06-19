package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KiteStockRefDto {

    private Long id;
    private String symbol;
    private String company;
    private String exchange;
    private String isinNumber;
}
