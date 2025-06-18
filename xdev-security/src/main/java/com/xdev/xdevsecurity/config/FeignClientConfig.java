package com.xdev.xdevsecurity.config;

import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String jwt) {
                log.info("Propagating JWT to downstream service: {}", jwt.substring(0, 10) + "...");
                requestTemplate.header("Authorization", "Bearer " + jwt);
            } else {
                log.warn("No JWT found in SecurityContext, proceeding without Authorization header");
            }
        };
    }
}