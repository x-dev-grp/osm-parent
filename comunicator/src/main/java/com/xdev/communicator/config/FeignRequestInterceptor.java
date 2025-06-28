package com.xdev.communicator.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(FeignRequestInterceptor.class);
    @Override
    public void apply(RequestTemplate template) {
        // Add correlation ID for request tracing
        String correlationId = getCorrelationId();
        template.header("X-Correlation-ID", correlationId);

        // Forward authorization header
        String authorization = getAuthorizationHeader();
        if (authorization != null) {
            template.header("Authorization", authorization);
        }

        // Add service identification
        template.header("X-Service-Name", getServiceName());

        log.debug("Feign request - Method: {}, URL: {}, Headers: {}",
                template.method(), template.url(), template.headers());
    }

    private String getCorrelationId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String correlationId = request.getHeader("X-Correlation-ID");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            return correlationId;
        }

        return UUID.randomUUID().toString();
    }

    private String getAuthorizationHeader() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Authorization");
        }

        return null;
    }

    private String getServiceName() {
        return System.getProperty("spring.application.name", "unknown-service");
    }
}