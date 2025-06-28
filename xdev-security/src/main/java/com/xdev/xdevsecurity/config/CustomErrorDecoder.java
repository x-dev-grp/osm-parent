package com.xdev.xdevsecurity.config;

 import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error - Method: {}, Status: {}, Reason: {}",
                methodKey, response.status(), response.reason());

        switch (response.status()) {
            case 400:
                return new Exception("Bad request to service: " + methodKey);
            case 401:
                return new Exception("Unauthorized access to service: " + methodKey);
            case 403:
                return new Exception("Forbidden access to service: " + methodKey);
            case 404:
                return new Exception("Service not found: " + methodKey);
            case 500:
                return new Exception("Internal server error in service: " + methodKey);
            case 503:
                return new Exception("Service unavailable: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}