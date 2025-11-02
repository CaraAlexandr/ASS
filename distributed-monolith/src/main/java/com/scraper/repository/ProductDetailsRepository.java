package com.scraper.repository;

import com.scraper.entity.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailsRepository extends JpaRepository<ProductDetails, Long> {
    Optional<ProductDetails> findByUrl(String url);
    boolean existsByUrl(String url);
}

