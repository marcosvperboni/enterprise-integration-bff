package com.marcosperboni.integrationbff.config;

import com.marcosperboni.integrationbff.config.properties.ClientsProperties;
import com.marcosperboni.integrationbff.web.filter.CorrelationIdWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * One WebClient per downstream system. Each carries the correlation-id
 * propagation filter so distributed traces stay linked without callers
 * having to remember to add the header themselves.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pricingServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.pricingService().baseUrl());
    }

    @Bean
    public WebClient inventoryServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.inventoryService().baseUrl());
    }

    @Bean
    public WebClient catalogServiceWebClient(ClientsProperties properties) {
        return buildClient(properties.catalogService().baseUrl());
    }

    private WebClient buildClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(correlationIdPropagationFilter())
                .build();
    }

    private ExchangeFilterFunction correlationIdPropagationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> Mono.deferContextual(ctx -> {
            String correlationId = ctx.getOrDefault(CorrelationIdWebFilter.CONTEXT_KEY, "n/a");
            ClientRequest withHeader = ClientRequest.from(request)
                    .header(CorrelationIdWebFilter.HEADER_NAME, correlationId)
                    .build();
            return Mono.just(withHeader);
        }));
    }
}
