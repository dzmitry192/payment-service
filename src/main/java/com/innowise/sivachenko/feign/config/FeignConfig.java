package com.innowise.sivachenko.feign.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.header("Is-Feign-Request", "true");
            requestTemplate.header("Authorization", "Bearer " + extractAccessToken());
        };
    }

    private String extractAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient authorizedClient = (OAuth2AuthorizedClient) authentication.getDetails();
        return authorizedClient.getAccessToken().getTokenValue();
    }
}
