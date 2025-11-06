package com.rakuten.mobile.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI (via springdoc) configuration:
 * - Adds global header params: Authorization (Bearer ...) and X-Tenant-Id
 * - Sets basic API metadata
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Survey Management API")
                        .version("v1")
                        .description("Multi-tenant Survey backend with JWT + tenant isolation"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public OpenApiCustomizer globalHeadersCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    // Add Authorization: Bearer <jwt>
                    op.addParametersItem(new HeaderParameter()
                            .name("Authorization")
                            .description("Bearer <JWT>")
                            .required(true));
                    // Add X-Tenant-Id
                    op.addParametersItem(new HeaderParameter()
                            .name("X-Tenant-Id")
                            .description("Tenant UUID that MUST match the JWT 'tenant' claim")
                            .required(true));
                })
        );
    }
}
