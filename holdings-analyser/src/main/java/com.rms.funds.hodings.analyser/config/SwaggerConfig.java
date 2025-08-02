package com.rms.funds.hodings.analyser.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mutual Fund API")
                        .version("1.0")
                        .description("API documentation for mutual fund application"));
    }

    // Optional: Grouped API
    @Bean
    public GroupedOpenApi fundGroup() {
        return GroupedOpenApi.builder()
                .group("funds")
                .pathsToMatch("/api/**")
                .build();
    }
}
