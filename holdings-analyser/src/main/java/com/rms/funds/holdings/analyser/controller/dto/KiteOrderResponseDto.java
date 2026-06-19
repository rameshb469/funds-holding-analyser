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
public class KiteOrderResponseDto {

    private String kiteOrderId;
    private String status;
    private String raw;
    private boolean sandbox;
    private Long auditId;
}
