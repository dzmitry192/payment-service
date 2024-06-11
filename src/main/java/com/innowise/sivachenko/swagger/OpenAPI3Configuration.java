package com.innowise.sivachenko.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class OpenAPI3Configuration {
    @Value("${openapi.oAuthFlow.tokenUrl}")
    private String tokenUrl;
    @Bean
    public OpenAPI customOpenApi(
            @Value("${openapi.service.name}") String appName,
            @Value("${openapi.service.version}") String serviceVersion,
            @Value("${openapi.service.description}") String appDescription,
            @Value("${openapi.service.url}") String url
    ) {
        String securitySchemeName = "Authorization";
        return new OpenAPI()
                .info(new Info().title(appName)
                        .version(serviceVersion)
                        .description(appDescription))
                .servers(List.of(new Server().url(url)))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, createOAuthScheme()))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }

    private SecurityScheme createOAuthScheme() {
        OAuthFlows flows = createPasswordOauthFlow();
        return new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(flows);
    }

    private OAuthFlows createPasswordOauthFlow() {
        OAuthFlow flow = createTokenUrlOAuthFlow();
        return new OAuthFlows().password(flow);
    }

    private OAuthFlow createTokenUrlOAuthFlow() {
        return new OAuthFlow()
                .tokenUrl(tokenUrl);
    }
}
