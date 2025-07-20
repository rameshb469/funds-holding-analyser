package com.rms.funds.hodings.analyser.modal;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    public static enum Status {
        SUCCESSFUL,
        FAILED;
    }

    private Long configId;
    private String name;
    private String downloadLink;
    private LocalDate atDate;
    private List<MutualFundStockHolding> holdings;
    private String error;
    private Status status;
}

