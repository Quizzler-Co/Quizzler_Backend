package com.onlinejudge.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {

    // CORS is disabled here because the API Gateway handles CORS
    // This prevents duplicate CORS headers when accessed through the gateway
    // If accessing the service directly (not through gateway), uncomment the bean below
    
    // @Bean
    // public CorsFilter corsFilter() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     CorsConfiguration config = new CorsConfiguration();
    //     
    //     // Allow React dev server on port 5173
    //     config.addAllowedOrigin("http://localhost:5173");
    //     
    //     // Allow all HTTP methods
    //     config.addAllowedMethod("*");
    //     
    //     // Allow all headers
    //     config.addAllowedHeader("*");
    //     
    //     // Allow credentials (cookies, authorization headers)
    //     config.setAllowCredentials(true);
    //     
    //     // Apply CORS configuration to all paths
    //     source.registerCorsConfiguration("/**", config);
    //     
    //     return new CorsFilter(source);
    // }
}

