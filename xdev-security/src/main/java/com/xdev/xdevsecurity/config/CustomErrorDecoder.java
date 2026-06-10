package com.xdev.xdevsecurity.config;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = readBody(response);
        log.error("Feign client error - Method: {}, Status: {}, Reason: {}, Body: {}",
                methodKey, response.status(), response.reason(), responseBody);

        switch (response.status()) {
            case 400:
                return new Exception(formatServiceError("Bad request to service", methodKey, responseBody));
            case 401:
                return new Exception(formatServiceError("Unauthorized access to service", methodKey, responseBody));
            case 403:
                return new Exception(formatServiceError("Forbidden access to service", methodKey, responseBody));
            case 404:
                return new Exception(formatServiceError("Service not found", methodKey, responseBody));
            case 500:
                return new Exception(formatServiceError("Internal server error in service", methodKey, responseBody));
            case 503:
                return new Exception(formatServiceError("Service unavailable", methodKey, responseBody));
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("Unable to read Feign error response body for {}", response.request().url(), e);
            return null;
        }
    }

    private String formatServiceError(String prefix, String methodKey, String responseBody) {
        String detail = extractErrorMessage(responseBody);
        if (detail != null && !detail.isBlank()) {
            return prefix + " (" + methodKey + "): " + detail;
        }
        return prefix + ": " + methodKey;
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        int marker = responseBody.indexOf("\"error\"");
        if (marker >= 0) {
            int firstQuote = responseBody.indexOf('"', responseBody.indexOf(':', marker) + 1);
            int secondQuote = firstQuote >= 0 ? responseBody.indexOf('"', firstQuote + 1) : -1;
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                return responseBody.substring(firstQuote + 1, secondQuote);
            }
        }

        String trimmed = responseBody.trim();
        return trimmed.length() > 300 ? trimmed.substring(0, 300) + "..." : trimmed;
    }
}