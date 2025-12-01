package com.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RouteLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("========================================");
        System.out.println("[ROUTE-LOGGER] Before routing");
        System.out.println("[ROUTE-LOGGER] Path: " + exchange.getRequest().getPath());
        System.out.println("[ROUTE-LOGGER] Method: " + exchange.getRequest().getMethod());
        System.out.println("[ROUTE-LOGGER] URI: " + exchange.getRequest().getURI());
        
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    System.out.println("[ROUTE-LOGGER] After routing - Success");
                    System.out.println("[ROUTE-LOGGER] Response status: " + exchange.getResponse().getStatusCode());
                    System.out.println("========================================");
                })
                .doOnError(error -> {
                    System.err.println("[ROUTE-LOGGER] After routing - ERROR!");
                    System.err.println("[ROUTE-LOGGER] Error: " + error.getClass().getName());
                    System.err.println("[ROUTE-LOGGER] Message: " + error.getMessage());
                    System.err.println("========================================");
                });
    }

    @Override
    public int getOrder() {
        return -2; // Run before GlobalErrorFilter
    }
}

