package com.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@Configuration
public class FallbackHandlerConfig {

    @Bean
    public RouterFunction<ServerResponse> fallbackRoutes() {

        Map<String, Object> userAuthFallbackBody = Map.of(
                "status", 503,
                "message", "UserAuth Service is unavailable. Please try again later.",
                "error", "SERVICE_UNAVAILABLE"
        );

        return RouterFunctions.route()

                // GET fallback
                .GET("/fallback/user-auth", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userAuthFallbackBody)
                )

                // POST fallback
                .POST("/fallback/user-auth", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userAuthFallbackBody)
                )

                .build();
    }
}

