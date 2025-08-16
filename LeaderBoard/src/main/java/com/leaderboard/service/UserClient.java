package com.leaderboard.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "USER-AUTH", configuration = com.leaderboard.config.FeignInterceptorConfig.class)
public interface UserClient {
    @PostMapping("/api/v1/auth/usernames")
    Map<String, String> getUserNames(@RequestBody List<Long> userIds);
}

