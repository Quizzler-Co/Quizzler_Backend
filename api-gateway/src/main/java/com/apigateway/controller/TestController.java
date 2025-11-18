package com.apigateway.controller;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway/test")
public class TestController {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    public TestController(DiscoveryClient discoveryClient, WebClient.Builder webClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/judge-direct")
    public Mono<ResponseEntity<Map<String, Object>>> testJudgeDirect() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test direct connection to judge service
            String directUrl = "http://127.0.0.1:8085/api/problems";
            result.put("testType", "Direct connection (bypassing gateway and Eureka)");
            result.put("url", directUrl);
            
            return webClientBuilder.build()
                    .get()
                    .uri(directUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(response -> {
                        result.put("status", "SUCCESS");
                        result.put("response", response);
                        return ResponseEntity.ok(result);
                    })
                    .onErrorResume(error -> {
                        result.put("status", "FAILED");
                        result.put("error", error.getMessage());
                        result.put("errorType", error.getClass().getSimpleName());
                        return Mono.just(ResponseEntity.status(500).body(result));
                    });
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            return Mono.just(ResponseEntity.status(500).body(result));
        }
    }

    @GetMapping("/judge-eureka")
    public ResponseEntity<Map<String, Object>> testJudgeEureka() {
        Map<String, Object> result = new HashMap<>();
        result.put("testType", "Eureka service discovery");
        
        if (discoveryClient != null) {
            try {
                var instances = discoveryClient.getInstances("JAVA-JUDGE");
                result.put("instancesFound", instances.size());
                
                if (instances.isEmpty()) {
                    result.put("status", "NOT_FOUND");
                    result.put("message", "JAVA-JUDGE service not found in Eureka");
                } else {
                    result.put("status", "FOUND");
                    var instance = instances.get(0);
                    result.put("host", instance.getHost());
                    result.put("port", instance.getPort());
                    result.put("uri", instance.getUri().toString());
                    result.put("serviceId", instance.getServiceId());
                }
            } catch (Exception e) {
                result.put("status", "ERROR");
                result.put("error", e.getMessage());
            }
        } else {
            result.put("status", "DISCOVERY_CLIENT_NOT_AVAILABLE");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/judge-submit-test")
    public Mono<ResponseEntity<Map<String, Object>>> testJudgeSubmit() {
        Map<String, Object> result = new HashMap<>();
        result.put("testType", "Test judge submit endpoint via load balancer");
        
        try {
            // Test connection to judge service via load balancer
            String testUrl = "lb://JAVA-JUDGE/api/problems";
            result.put("url", testUrl);
            
            return webClientBuilder.build()
                    .get()
                    .uri(testUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(response -> {
                        result.put("status", "SUCCESS");
                        result.put("responseLength", response != null ? response.length() : 0);
                        result.put("message", "Judge service is reachable via load balancer");
                        return ResponseEntity.ok(result);
                    })
                    .onErrorResume(error -> {
                        result.put("status", "FAILED");
                        result.put("error", error.getMessage());
                        result.put("errorType", error.getClass().getSimpleName());
                        result.put("message", "Judge service is NOT reachable via load balancer");
                        return Mono.just(ResponseEntity.status(500).body(result));
                    });
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            return Mono.just(ResponseEntity.status(500).body(result));
        }
    }
}

