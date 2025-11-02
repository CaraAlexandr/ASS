package com.scraper.consumer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Consumer Service API")
                        .version("1.0.0")
                        .description("Consumer service that consumes URLs from RabbitMQ and saves to PostgreSQL")
                        .contact(new Contact()
                                .name("Scraper Team")
                                .email("scraper@example.com")));
    }
}

