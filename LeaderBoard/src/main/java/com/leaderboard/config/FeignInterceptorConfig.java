
package com.leaderboard.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignInterceptorConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        System.out.println("Adding x-internal-call header");
        template.header("x-internal-call", "true");

    }
}