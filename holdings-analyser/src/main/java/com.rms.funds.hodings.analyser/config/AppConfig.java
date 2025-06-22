package com.rms.funds.hodings.analyser.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

@Configuration
public class AppConfig {

    private static final Integer CONNECTION_TIME_OUT = 2;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    @Bean(name = "nipponMFApiRestTemplate")
    public RestTemplate nipponMFApiRestTemplate(RestTemplateBuilder builder) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = new ClassPathResource("certs/nipponmf-truststore.jks").getInputStream()) {
            trustStore.load(is, "changeit".toCharArray());
        }

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, (X509Certificate[] chain, String authType) -> true)
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(
                                SSLConnectionSocketFactoryBuilder.create()
                                        .setSslContext(sslContext)
                                        .build()
                        )
                        .build()
                )
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

}
