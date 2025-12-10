package com.scraper.producer.config;

import com.scraper.producer.pattern.structural.adapter.MessageAdapter;
import com.scraper.producer.pattern.structural.facade.ScrapingFacade;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for design pattern beans
 */
@Configuration
public class PatternConfig {
    
    @Bean
    public MessageAdapter messageAdapter() {
        return new MessageAdapter();
    }
    
    @Bean
    public ScrapingFacade scrapingFacade(RabbitTemplate rabbitTemplate) {
        return new ScrapingFacade(rabbitTemplate);
    }
}

