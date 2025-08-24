package com.rms.funds.hodings.analyser.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractorJobResponse {

    private Long id;
    private Long mutualFundId;
    private String mutualFundName;
    private String url;
    private LocalDate atDate;
    private String status;
    private String error;
    private Integer recordCount;
}
