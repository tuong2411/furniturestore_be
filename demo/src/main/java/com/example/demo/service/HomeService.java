package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.HomeCategoryDto;
import com.example.demo.dto.HomeProductDto;
import com.example.demo.dto.HomeResponse;
import com.example.demo.repository.HomeRepository;

@Service
public class HomeService {

	private final HomeRepository homeRepository;

    public HomeService(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public HomeResponse getHome() {
        List<HomeCategoryDto> categories = homeRepository.findTopParentCategories(4);
        List<HomeProductDto> featured = homeRepository.findFeaturedProducts(8);
        return new HomeResponse(categories, featured);
    }
}
