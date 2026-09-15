package com.marcosperboni.integrationbff.web.filter;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Assigns a correlation id to every request (reusing the caller's
 * X-Correlation-Id header when present), echoes it back on the response, and
 * stores it in the Reactor context so downstream WebClient calls and log
 * lines can pick it up.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String CONTEXT_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().add(HEADER_NAME, correlationId);

        String finalCorrelationId = correlationId;
        return chain.filter(exchange).contextWrite(ctx -> ctx.put(CONTEXT_KEY, finalCorrelationId));
    }
}
