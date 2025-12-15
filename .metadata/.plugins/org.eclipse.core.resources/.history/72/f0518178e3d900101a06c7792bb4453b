package com.example.demo.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ProductDetailDto;
import com.example.demo.repository.ProductDetailRepository;

@Service
public class ProductDetailService {

    private final ProductDetailRepository repo;

    public ProductDetailService(ProductDetailRepository repo) {
        this.repo = repo;
    }

    public ProductDetailDto getBySlug(String slug) {
        Map<String, Object> p = repo.findProductBySlug(slug)
            .orElseThrow(() -> new NoSuchElementException("PRODUCT_NOT_FOUND"));

        long productId = ((Number) p.get("product_id")).longValue();
        long categoryId = ((Number) p.get("category_id")).longValue();

        ProductDetailDto dto = new ProductDetailDto();
        dto.id = productId;
        dto.name = (String) p.get("name");
        dto.slug = (String) p.get("slug");
        dto.sku = (String) p.get("sku");

        dto.price = (BigDecimal) p.get("base_price");
        dto.originalPrice = null; // ✅ DB chưa có cột này (fen có thể thêm sau)
        dto.mainImage = (String) p.get("main_image");
        dto.description = (String) p.get("description");

        dto.rating = 4.8;      // tạm
        dto.reviewCount = 125; // tạm

        dto.gallery = repo.findGallery(productId);

        // ===== Variants =====
        List<Map<String, Object>> vars = repo.findVariants(productId);

        ProductDetailDto.VariantDto vdto = new ProductDetailDto.VariantDto();

        // Colors: lấy unique từ DB
        List<ProductDetailDto.ColorDto> colors = vars.stream()
            .map(r -> (String) r.get("color"))
            .filter(Objects::nonNull)
            .distinct()
            .map(name -> {
                ProductDetailDto.ColorDto c = new ProductDetailDto.ColorDto();
                c.name = name;
                c.hex = "#F5F5DC"; // chưa có hex trong DB -> tạm 1 màu
                return c;
            })
            .toList();

        // Nếu DB không có color, fallback
        if (colors.isEmpty()) {
            ProductDetailDto.ColorDto c = new ProductDetailDto.ColorDto();
            c.name = "Mặc định";
            c.hex = "#F5F5DC";
            colors = List.of(c);
        }
        vdto.colors = colors;

        // Sizes: group theo size_label, lấy giá nhỏ nhất cho mỗi size
        Map<String, BigDecimal> minPriceBySize = new LinkedHashMap<>();
        for (Map<String, Object> r : vars) {
            String label = (String) r.get("size_label"); // alias từ SQL (size AS size_label)
            BigDecimal price = (BigDecimal) r.get("price");
            if (label == null) label = "Tiêu chuẩn";
            minPriceBySize.merge(label, price, (a, b) -> a.min(b));
        }

        List<ProductDetailDto.SizeDto> sizes = minPriceBySize.entrySet().stream()
            .map(e -> {
                ProductDetailDto.SizeDto s = new ProductDetailDto.SizeDto();
                s.label = e.getKey();
                s.price = e.getValue() != null ? e.getValue() : dto.price;
                return s;
            })
            .collect(Collectors.toList());

        // nếu không có variant -> fallback 1 size
        if (sizes.isEmpty()) {
            ProductDetailDto.SizeDto s = new ProductDetailDto.SizeDto();
            s.label = "Tiêu chuẩn";
            s.price = dto.price;
            sizes = List.of(s);
        }
        vdto.sizes = sizes;

        dto.variants = vdto;

        dto.specs = Map.of(
            "Chất liệu bọc", "Vải Bouclé",
            "Khung", "Gỗ sồi tự nhiên",
            "Đệm", "Mút D40 + lò xo túi",
            "Chân ghế", "Gỗ sồi sơn mờ"
        );

        dto.relatedProducts = repo.findRelated(categoryId, productId).stream().map(r -> {
            ProductDetailDto.RelatedProductDto rp = new ProductDetailDto.RelatedProductDto();
            rp.id = ((Number) r.get("product_id")).longValue();
            rp.name = (String) r.get("name");
            rp.slug = (String) r.get("slug");
            rp.price = (BigDecimal) r.get("base_price");
            rp.image = (String) r.get("main_image");
            return rp;
        }).toList();

        return dto;
    }
}
