package com.xdev.communicator.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class FeignConfiguration {
//
//    @Bean
//    public Logger.Level feignLoggerLevel() {
//        return Logger.Level.HEADERS;
//    }
//
//    @Bean
//    public Request.Options requestOptions() {
//        return new Request.Options(
//                5000,  // connectTimeout
//                30000, // readTimeout
//                true   // followRedirects
//        );
//    }
//
//    @Bean
//    public Retryer feignRetryer() {
//        return new Retryer.Default(
//                100,    // period
//                1000,   // maxPeriod
//                3       // maxAttempts
//        );
//    }
//
//    @Bean
//    public ErrorDecoder errorDecoder() {
//        return new CustomErrorDecoder();
//    }
//}
