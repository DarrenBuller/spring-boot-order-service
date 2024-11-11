package com.example.microservices.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {
        @Bean
        public OpenAPI openApi() {
                return new OpenAPI()
                                .info(new Info().title("Order Service API")
                                                .description("This is the REST API for Order Service")
                                                .version("v0.0.1")
                                                .contact(new Contact()
                                                                .name("Dawn Phobia Flock")
                                                                .url("https://dflock.com")
                                                                .email("dawn-phobia-flock@duck.com"))
                                                .license(new License().name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("You can refer to the Order Service Additional Documentation")
                                                .url("https://service-dummy-url.com/docs"));
        }
}
