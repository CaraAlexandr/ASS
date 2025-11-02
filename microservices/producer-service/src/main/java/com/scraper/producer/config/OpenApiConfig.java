package com.scraper.producer.config;

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
                        .title("Producer Service API")
                        .version("1.0.0")
                        .description("Producer service that scrapes eBay and publishes URLs to RabbitMQ")
                        .contact(new Contact()
                                .name("Scraper Team")
                                .email("scraper@example.com")));
    }
}

