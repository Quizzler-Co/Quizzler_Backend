package com.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalErrorFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    // Log successful responses
                    System.out.println("[API-GATEWAY] Request completed successfully: " + 
                        exchange.getRequest().getPath() + " -> " + 
                        exchange.getResponse().getStatusCode());
                })
                .onErrorResume(throwable -> {
                    // Log errors
                    System.err.println("========================================");
                    System.err.println("[API-GATEWAY] ERROR occurred!");
                    System.err.println("[API-GATEWAY] Path: " + exchange.getRequest().getPath());
                    System.err.println("[API-GATEWAY] Method: " + exchange.getRequest().getMethod());
                    System.err.println("[API-GATEWAY] Error type: " + throwable.getClass().getName());
                    System.err.println("[API-GATEWAY] Error message: " + throwable.getMessage());
                    if (throwable.getCause() != null) {
                        System.err.println("[API-GATEWAY] Cause: " + throwable.getCause().getClass().getName());
                        System.err.println("[API-GATEWAY] Cause message: " + throwable.getCause().getMessage());
                    }
                    throwable.printStackTrace();
                    System.err.println("========================================");
                    
                    // Return error response - only if response is not committed
                    ServerHttpResponse response = exchange.getResponse();
                    if (!response.isCommitted()) {
                        // Set status and content type BEFORE writing
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        
                        String errorMessage = "{\"status\":500,\"message\":\"" + 
                            (throwable.getMessage() != null ? throwable.getMessage().replace("\"", "\\\"").replace("\n", "\\n") : "Internal Server Error") + 
                            "\",\"error\":\"" + 
                            throwable.getClass().getSimpleName() + "\"}";
                        
                        byte[] responseBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
                        
                        return response.writeWith(Mono.just(buffer));
                    } else {
                        // Response already committed, can't modify
                        return Mono.empty();
                    }
                });
    }

    @Override
    public int getOrder() {
        return -1; // High priority - run early
    }
}

