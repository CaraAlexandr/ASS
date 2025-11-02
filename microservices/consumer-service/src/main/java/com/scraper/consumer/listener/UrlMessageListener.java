package com.scraper.consumer.listener;

import com.scraper.consumer.dto.ProductInfo;
import com.scraper.consumer.service.ProductExtractorService;
import com.scraper.consumer.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrlMessageListener {
    
    private final ProductExtractorService extractorService;
    private final ProductService productService;
    
    @RabbitListener(queues = "${queue.name}")
    public void handleMessage(org.springframework.amqp.core.Message message) {
        String url = new String(message.getBody());
        log.info("Received URL: {}", url);
        
        try {
            ProductInfo productInfo = extractorService.extractProductInfo(url);
            productService.saveProduct(url, productInfo);
            log.info("Successfully processed URL: {}", url);
        } catch (Exception e) {
            log.error("Error processing URL {}: {}", url, e.getMessage());
            // In a production environment, you might want to send to a dead letter queue
            throw new RuntimeException("Failed to process URL: " + url, e);
        }
    }
}

