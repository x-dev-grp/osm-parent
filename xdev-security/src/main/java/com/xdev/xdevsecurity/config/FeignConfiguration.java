package com.xdev.xdevsecurity.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfiguration {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignConfiguration.class);

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5000,  // connectTimeout
                30000, // readTimeout
                true   // followRedirects
        );
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                100,    // period
                1000,   // maxPeriod
                3       // maxAttempts
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

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
