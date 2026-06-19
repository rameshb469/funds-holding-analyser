package com.rms.funds.holdings.analyser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kite")
@Getter
@Setter
public class KiteConfigProperties {

    private String apiKey;
    private String apiSecret;
    private String baseUrl = "https://api.kite.trade";
    private String loginRedirectUri = "http://127.0.0.1:8080/api/broker/callback";
    private boolean sandbox = false;
}
