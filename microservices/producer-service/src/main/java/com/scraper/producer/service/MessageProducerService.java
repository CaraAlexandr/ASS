package com.scraper.producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${queue.name}")
    private String queueName;
    
    public void sendUrl(String url) {
        try {
            rabbitTemplate.convertAndSend(queueName, url.getBytes());
            log.info("Sent URL to queue: {}", url);
        } catch (Exception e) {
            log.error("Error sending URL to queue: {}", e.getMessage());
        }
    }
    
    public void sendUrls(List<String> urls) {
        urls.forEach(this::sendUrl);
        log.info("Sent {} URLs to queue", urls.size());
    }
}

