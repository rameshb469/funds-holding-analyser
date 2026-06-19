package com.rms.funds.holdings.analyser.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KiteSessionStatusDto {

    private boolean connected;
    private String userId;
    private String userName;
    private LocalDateTime loginAt;
    private LocalDateTime expiresAt;
    private boolean sandbox;
}
