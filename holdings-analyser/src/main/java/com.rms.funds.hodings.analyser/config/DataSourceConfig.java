package com.rms.funds.hodings.analyser.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() throws Exception {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            URI dbUri = new URI(databaseUrl);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        // Fallback for local development (uses DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)
        return DataSourceBuilder.create()
                .url(System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/funds-holding-analyser"))
                .username(System.getenv().getOrDefault("DATABASE_USERNAME", "postgres"))
                .password(System.getenv().getOrDefault("DATABASE_PASSWORD", "root"))
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
