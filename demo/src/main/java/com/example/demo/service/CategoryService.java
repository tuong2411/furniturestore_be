package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CategoryTreeDto;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CategoryRepository.CategoryRow;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryTreeDto> getTree() {
        List<CategoryRow> rows = categoryRepository.findAllActive();

        Map<Long, CategoryTreeDto> byId = new HashMap<>();
        for (CategoryRow r : rows) {
            byId.put(r.categoryId, r.toDto());
        }

        List<CategoryTreeDto> roots = new ArrayList<>();
        for (CategoryRow r : rows) {
            CategoryTreeDto node = byId.get(r.categoryId);
            if (r.parentId == null) {
                roots.add(node);
            } else {
                CategoryTreeDto parent = byId.get(r.parentId);
                if (parent != null) parent.addChild(node);
                else roots.add(node);
            }
        }

        return roots;
    }

}
