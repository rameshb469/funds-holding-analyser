package com.rms.funds.hodings.analyser.test;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    private String name;
    private String downloadLink;
    private String atDate;
    private int count;

    private Long mutualFundId;
    private String error;
    private String status;
}
