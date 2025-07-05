package com.xdev.xdevsecurity.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

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
                10000,  // connectTimeout
                60000,  // readTimeout
                true    // followRedirects
        );
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                1000,   // period
                5000,   // maxPeriod
                5       // maxAttempts
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.debug("Feign interceptor called for URL: {}", requestTemplate.url());

            // First try to get the token from the current request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    log.debug("Propagating Authorization header from current request");
                    requestTemplate.header("Authorization", authHeader);
                    return;
                } else {
                    log.debug("No Authorization header found in current request");
                    // Log all headers for debugging
                    Enumeration<String> headerNames = request.getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        log.debug("Request header: {} = {}", headerName, request.getHeader(headerName));
                    }
                }
            } else {
                log.debug("No RequestContextHolder attributes found");
            }

            // Fallback to SecurityContextHolder
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                log.debug("Authentication found: principal={}, credentials={}, authenticated={}",
                        authentication.getPrincipal(),
                        authentication.getCredentials(),
                        authentication.isAuthenticated());

                if (authentication.getCredentials() instanceof String jwt) {
                    log.debug("Propagating JWT from SecurityContext credentials: {}", jwt.substring(0, Math.min(10, jwt.length())) + "...");
                    requestTemplate.header("Authorization", "Bearer " + jwt);
                    return;
                } else if (authentication.getPrincipal() instanceof Jwt jwt) {
                    log.debug("Propagating JWT from SecurityContext principal");
                    requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
                    return;
                } else if (authentication.getPrincipal() instanceof String principal) {
                    // Sometimes the JWT is stored as the principal
                    if (principal.startsWith("eyJ")) { // JWT tokens start with "eyJ"
                        log.debug("Propagating JWT from SecurityContext principal (String)");
                        requestTemplate.header("Authorization", "Bearer " + principal);
                        return;
                    }
                }

                // Try to extract from authorities or other authentication properties
                log.debug("Authentication authorities: {}", authentication.getAuthorities());
                log.debug("Authentication details: {}", authentication.getDetails());
            } else {
                log.debug("No Authentication found in SecurityContext");
            }

            log.warn("No JWT found in SecurityContext or current request, proceeding without Authorization header");
            log.warn("This may cause 401 Unauthorized errors in downstream services");
        };
    }

    /**
     * Custom executor that propagates security context for CompletableFuture operations
     */
    @Bean
    public Executor feignExecutor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        SecurityContextHolder.setContext(securityContext);
                        log.debug("Security context propagated to Feign executor thread: {}", Thread.currentThread().getName());
                        command.run();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                });
            }
        };
    }
}
