package main.java.com.payments.service.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI paymentsServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payments Service API")
                        .description("Fleet Management Payments and Subscriptions Service")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Payments Service Documentation")
                        .url("https://payments-service.docs"));
    }
}
