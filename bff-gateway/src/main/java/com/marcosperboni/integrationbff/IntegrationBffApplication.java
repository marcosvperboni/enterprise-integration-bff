package com.marcosperboni.integrationbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IntegrationBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationBffApplication.class, args);
    }
}
