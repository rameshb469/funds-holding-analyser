package com.rms.funds.holdings.analyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FundsHoldingAnalyserApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundsHoldingAnalyserApplication.class, args);
	}

}
