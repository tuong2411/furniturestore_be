package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.PagingDto;
import com.example.demo.dto.ProductListItemDto;
import com.example.demo.dto.ProductListResponse;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
	private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductListResponse getProducts(
            String q,
            String category,
            List<String> materials,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer page,
            Integer size,
            String sort
    ) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 60) ? 12 : size;

        long total = productRepository.countProducts(q, category, materials, minPrice, maxPrice);
        List<ProductListItemDto> items = productRepository.findProducts(q, category, materials, minPrice, maxPrice, p, s, sort);

        return new ProductListResponse(items, new PagingDto(p, s, total));
    }

}
