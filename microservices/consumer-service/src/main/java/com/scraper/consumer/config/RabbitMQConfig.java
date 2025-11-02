package com.scraper.consumer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${queue.name}")
    private String queueName;
    
    @Bean
    public Queue urlQueue() {
        return new Queue(queueName, true); // durable queue
    }
    
    @Bean
    public MessageConverter messageConverter() {
        // Using default message converter for plain string messages
        return new org.springframework.amqp.support.converter.SimpleMessageConverter();
    }
}

