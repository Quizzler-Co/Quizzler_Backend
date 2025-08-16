package com.apigateway.filter;

import com.apigateway.DTO.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(RouteValidator route,WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.validator = route;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println(" Path: " + exchange.getRequest().getPath());
            if (validator.isSecured.test(exchange.getRequest())) {
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Authorization header is missing or doesn't start with Bearer"
                    ));
                }

                System.out.println(" Auth Header: " + authHeader);
                String token = authHeader.substring(7);

                return webClientBuilder.build()
                        .get()
                        .uri("http://USER-AUTH/api/v1/auth/validate?token=" + token)
                        .retrieve()
                        .onStatus(
                                status -> status.is5xxServerError(),
                                clientResponse -> clientResponse
                                        .bodyToMono(String.class)
                                        .doOnNext(errorBody -> System.err.println("Auth service error: " + errorBody))
                                        .defaultIfEmpty("Authentication service unavailable")
                                        .flatMap(errorBody ->
                                                Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Authentication service unavailable"))
                                        )
                        )

                        .bodyToMono(UserValidationResponse.class)
                        .flatMap(response -> {
                            // Add headers to forward user info downstream
                            return chain.filter(
                                    exchange.mutate()
                                            .request(r -> r.headers(headers -> {
                                                headers.set("x-user-id", response.getUserId());
                                                headers.set("x-email", response.getEmail());
                                                headers.set("x-role", response.getRole());
                                            }))
                                            .build()
                            );
                        });


            }
            System.out.println("In GatewayFilter");
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
