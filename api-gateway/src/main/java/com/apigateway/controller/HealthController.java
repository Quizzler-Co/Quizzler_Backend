package com.apigateway.controller;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    private final DiscoveryClient discoveryClient;

    public HealthController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "API Gateway is running on port 8086");
        health.put("eurekaDashboard", "http://localhost:8761");
        
        // Check service discovery
        if (discoveryClient != null) {
            try {
                List<String> services = discoveryClient.getServices();
                health.put("totalServices", services.size());
                
                // Check specifically for JAVA-JUDGE
                boolean javaJudgeFound = services.stream()
                        .anyMatch(s -> s.equalsIgnoreCase("JAVA-JUDGE"));
                health.put("javaJudgeRegistered", javaJudgeFound);
                
                if (javaJudgeFound) {
                    var instances = discoveryClient.getInstances("JAVA-JUDGE");
                    health.put("javaJudgeInstances", instances.size());
                    if (!instances.isEmpty()) {
                        var instance = instances.get(0);
                        health.put("javaJudgeUri", instance.getUri().toString());
                        health.put("javaJudgeHost", instance.getHost());
                        health.put("javaJudgePort", instance.getPort());
                    }
                }
                
                // List all registered services
                Map<String, Integer> serviceCounts = services.stream()
                        .collect(Collectors.toMap(
                                s -> s,
                                s -> discoveryClient.getInstances(s).size()
                        ));
                health.put("registeredServices", serviceCounts);
            } catch (Exception e) {
                health.put("discoveryError", e.getMessage());
            }
        } else {
            health.put("discoveryClient", "Not available");
        }
        
        return ResponseEntity.ok(health);
    }
}

