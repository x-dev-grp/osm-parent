package com.xdev.xdevsecurity.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig {
    String[] WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-resources",
            "/webjars/**",
            "/configuration/**",
            "/actuator/**"
    };
    private static final Logger log = LoggerFactory.getLogger(ResourceServerConfig.class);

    @Value("${spring.security.oauth2.resource-server.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> new CorsConfig())
                .authorizeHttpRequests(registry -> {
                    registry
                            .anyRequest().permitAll();
                })
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
       return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .build();


    }
}
