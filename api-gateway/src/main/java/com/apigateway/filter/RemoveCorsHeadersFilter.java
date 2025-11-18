package com.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class RemoveCorsHeadersFilter extends AbstractGatewayFilterFactory<RemoveCorsHeadersFilter.Config> {

    public RemoveCorsHeadersFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).doOnSuccess(aVoid -> {
                var response = exchange.getResponse();
                if (!response.isCommitted()) {
                    HttpHeaders headers = response.getHeaders();
                    
                    // Remove CORS headers that might have been added by downstream services
                    // The gateway's global CORS configuration will add them back
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                    }
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                    }
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
                    }
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
                    }
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
                    }
                    if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_MAX_AGE)) {
                        headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
                    }
                }
            });
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}

