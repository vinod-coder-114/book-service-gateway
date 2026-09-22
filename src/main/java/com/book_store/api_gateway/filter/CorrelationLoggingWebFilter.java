package com.book_store.api_gateway.filter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorrelationLoggingWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getId();
        String correlationId = resolveCorrelationId(request, requestId);

        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    if (headers.getFirst(CORRELATION_ID_HEADER) == null) {
                        headers.set(CORRELATION_ID_HEADER, correlationId);
                    }
                })
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        long startedAtNanos = System.nanoTime();
        log.info("Gateway request start: correlationId={}, requestId={}, method={}, path={}",
                correlationId,
                requestId,
                request.getMethod(),
                request.getURI().getRawPath());

        return chain.filter(mutatedExchange)
                .doOnError(error -> log.error(
                        "Gateway request error: correlationId={}, requestId={}, method={}, path={}, message={}",
                        correlationId,
                        requestId,
                        request.getMethod(),
                        request.getURI().getRawPath(),
                        error.getMessage(),
                        error))
                .doFinally(signalType -> {
                    HttpStatusCode statusCode = mutatedExchange.getResponse().getStatusCode();
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos);
                    log.info("Gateway request end: correlationId={}, requestId={}, method={}, path={}, status={}, durationMs={}, signal={}",
                            correlationId,
                            requestId,
                            request.getMethod(),
                            request.getURI().getRawPath(),
                            statusCode != null ? statusCode.value() : "NA",
                            durationMs,
                            signalType);
                });
    }

    private String resolveCorrelationId(ServerHttpRequest request, String requestId) {
        return Optional.ofNullable(request.getHeaders().getFirst(CORRELATION_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElse(requestId);
    }
}
