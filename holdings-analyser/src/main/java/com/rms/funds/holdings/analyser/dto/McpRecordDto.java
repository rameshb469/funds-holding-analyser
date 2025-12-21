package com.rms.funds.holdings.analyser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McpRecordDto {
    // Only keep the requested columns
    private String symbol;
    private String series;
    private BigDecimal marketCapRs;
    private BigDecimal faceValueRs;
}
