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
        dto.originalPrice = null; 
        dto.mainImage = (String) p.get("main_image");
        dto.description = (String) p.get("description");

        dto.rating = 4.8;      
        dto.reviewCount = 125; 

        dto.gallery = repo.findGallery(productId);

        // ===== Variants =====
        List<Map<String, Object>> vars = repo.findVariants(productId);

     // 1. variantList (để FE tìm variantId)
	     dto.variantList = vars.stream().map(r -> {
	         ProductDetailDto.VariantItemDto v = new ProductDetailDto.VariantItemDto();
	         Object vid = r.get("variant_id");
	         v.variantId = (vid == null) ? null : ((Number) vid).longValue();
	         v.material = (String) r.get("material");
	         v.color = (String) r.get("color");
	         v.size = (String) r.get("size");
	         v.price = (BigDecimal) r.get("price");
	         return v;
	     }).toList();
	
	     // 2. UI options (colors + sizes)
	     ProductDetailDto.VariantUiDto vdto = new ProductDetailDto.VariantUiDto();
	
	     // map màu → hex
	     Map<String, String> colorHexMap = Map.of(
	         "Trắng Kem", "#F5F5DC",
	         "Kem", "#F5F5DC",
	         "Nâu", "#D2B48C",
	         "Xám", "#808080",
	         "Đen", "#111827",
	         "Trắng", "#F9FAFB"
	     );
	
	     // colors unique
	     vdto.colors = dto.variantList.stream()
	         .map(it -> it.color)
	         .filter(c -> c != null && !c.isBlank())
	         .distinct()
	         .map(c -> {
	             ProductDetailDto.ColorDto cd = new ProductDetailDto.ColorDto();
	             cd.name = c;
	             cd.hex = colorHexMap.getOrDefault(c, "#CBD5E1");
	             return cd;
	         })
	         .toList();
	
	     // sizes unique
	     vdto.sizes = dto.variantList.stream()
	         .map(it -> it.size)
	         .filter(s -> s != null && !s.isBlank())
	         .distinct()
	         .map(s -> {
	             ProductDetailDto.SizeDto sd = new ProductDetailDto.SizeDto();
	             sd.label = s;
	             return sd;
	         })
	         .toList();
	
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
