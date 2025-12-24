package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.admin.AdminProductUpsertRequest;
import com.example.demo.dto.admin.ProductImageRequest;
import com.example.demo.dto.admin.VariantUpsertRequest;
import com.example.demo.repository.AdminProductRepository;

@Service
public class AdminProductService {
	private final AdminProductRepository repo;

	  public AdminProductService(AdminProductRepository repo) {
	    this.repo = repo;
	  }

	  public Map<String, Object> list(String keyword, Long categoryId, Boolean active, int page, int size) {
	    int safePage = Math.max(0, page);
	    int safeSize = Math.min(100, Math.max(1, size));
	    int offset = safePage * safeSize;

	    long total = repo.countProducts(keyword, categoryId, active);
	    List<Map<String, Object>> items = repo.findProducts(keyword, categoryId, active, safeSize, offset);

	    Map<String, Object> resp = new HashMap<>();
	    resp.put("items", items);
	    resp.put("page", safePage);
	    resp.put("size", safeSize);
	    resp.put("total", total);
	    resp.put("totalPages", (int) Math.ceil(total * 1.0 / safeSize));
	    return resp;
	  }

	  public Map<String, Object> detail(long productId) {
	    var p = repo.findProductById(productId).orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
	    var images = repo.findImages(productId);
	    var variants = repo.findVariants(productId);
	    var styles = repo.findStylesOfProduct(productId);

	    Map<String, Object> resp = new HashMap<>();
	    resp.put("product", p);
	    resp.put("images", images);
	    resp.put("variants", variants);
	    resp.put("styles", styles);
	    return resp;
	  }

	  @Transactional
	  public Map<String, Object> create(AdminProductUpsertRequest req) {
	    validateUpsert(req, null);

	    long newId = repo.insertProduct(
	      req.categoryId,
	      req.name,
	      req.slug,
	      req.sku,
	      req.shortDesc,
	      req.description,
	      req.basePrice,
	      req.baseStock == null ? 0 : req.baseStock,
	      req.mainImage,
	      req.isActive != null && req.isActive
	    );

	    return Map.of("productId", newId);
	  }

	  @Transactional
	  public Map<String, Object> update(long productId, AdminProductUpsertRequest req) {
	    // must exist
	    repo.findProductById(productId).orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));

	    validateUpsert(req, productId);

	    int updated = repo.updateProduct(
	      productId,
	      req.categoryId,
	      req.name,
	      req.slug,
	      req.sku,
	      req.shortDesc,
	      req.description,
	      req.basePrice,
	      req.baseStock == null ? 0 : req.baseStock,
	      req.mainImage,
	      req.isActive != null && req.isActive
	    );
	    if (updated == 0) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
	    return Map.of("message", "OK");
	  }

	  public Map<String, Object> updateActive(long productId, boolean isActive) {
	    int updated = repo.updateActive(productId, isActive);
	    if (updated == 0) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
	    return Map.of("message", "OK");
	  }

	  // ===== Images =====

	  @Transactional
	  public Map<String, Object> addImage(long productId, ProductImageRequest req) {
	    repo.findProductById(productId).orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
	    if (req == null || req.imageUrl == null || req.imageUrl.isBlank()) throw new IllegalArgumentException("IMAGE_URL_REQUIRED");

	    int isMain = (req.isMain != null && req.isMain == 1) ? 1 : 0;
	    int order = req.displayOrder == null ? 0 : req.displayOrder;

	    if (isMain == 1) repo.clearMainImages(productId);
	    long imageId = repo.insertImage(productId, req.imageUrl, isMain, order);

	    // nếu set main => update products.main_image luôn cho FE tiện
	    if (isMain == 1) repo.updateProductMainImage(productId, req.imageUrl);

	    return Map.of("imageId", imageId);
	  }

	  @Transactional
	  public Map<String, Object> setMainImage(long productId, long imageId) {
	    repo.findProductById(productId).orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));

	    // lấy url của image để sync sang products.main_image
	    var imgs = repo.findImages(productId);
	    String url = null;
	    for (var m : imgs) {
	      if (((Number) m.get("image_id")).longValue() == imageId) {
	        url = (String) m.get("image_url");
	        break;
	      }
	    }
	    if (url == null) throw new IllegalArgumentException("IMAGE_NOT_FOUND");

	    repo.clearMainImages(productId);
	    int ok = repo.setMainImage(productId, imageId);
	    if (ok == 0) throw new IllegalArgumentException("IMAGE_NOT_FOUND");

	    repo.updateProductMainImage(productId, url);
	    return Map.of("message", "OK");
	  }

	  public Map<String, Object> deleteImage(long productId, long imageId) {
	    int ok = repo.deleteImage(productId, imageId);
	    if (ok == 0) throw new IllegalArgumentException("IMAGE_NOT_FOUND");
	    return Map.of("message", "OK");
	  }

	  // ===== Variants =====

	  @Transactional
	  public Map<String, Object> addVariant(long productId, VariantUpsertRequest req) {
	    repo.findProductById(productId).orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
	    validateVariant(req, null);

	    if (repo.existsVariantSku(req.sku, null)) throw new IllegalStateException("VARIANT_SKU_EXISTS");

	    long id = repo.insertVariant(
	      productId,
	      req.sku,
	      req.color,
	      req.size,
	      req.material,
	      req.extraDesc,
	      req.price,
	      req.stock == null ? 0 : req.stock,
	      req.isActive != null && req.isActive
	    );
	    return Map.of("variantId", id);
	  }

	  @Transactional
	  public Map<String, Object> updateVariant(long variantId, VariantUpsertRequest req) {
	    var v = repo.findVariantById(variantId).orElseThrow(() -> new IllegalArgumentException("VARIANT_NOT_FOUND"));
	    validateVariant(req, variantId);

	    if (repo.existsVariantSku(req.sku, variantId)) throw new IllegalStateException("VARIANT_SKU_EXISTS");

	    int ok = repo.updateVariant(
	      variantId,
	      req.sku,
	      req.color,
	      req.size,
	      req.material,
	      req.extraDesc,
	      req.price,
	      req.stock == null ? 0 : req.stock,
	      req.isActive != null && req.isActive
	    );
	    if (ok == 0) throw new IllegalArgumentException("VARIANT_NOT_FOUND");
	    return Map.of("message", "OK");
	  }

	  public Map<String, Object> updateVariantActive(long variantId, boolean isActive) {
	    int ok = repo.updateVariantActive(variantId, isActive);
	    if (ok == 0) throw new IllegalArgumentException("VARIANT_NOT_FOUND");
	    return Map.of("message", "OK");
	  }

	  // ===== validation =====

	  private void validateUpsert(AdminProductUpsertRequest req, Long excludeProductId) {
	    if (req == null) throw new IllegalArgumentException("INVALID_BODY");
	    if (req.categoryId == null) throw new IllegalArgumentException("CATEGORY_REQUIRED");
	    if (!repo.categoryExists(req.categoryId)) throw new IllegalArgumentException("CATEGORY_NOT_FOUND");

	    if (req.name == null || req.name.isBlank()) throw new IllegalArgumentException("NAME_REQUIRED");
	    if (req.slug == null || req.slug.isBlank()) throw new IllegalArgumentException("SLUG_REQUIRED");
	    if (req.sku == null || req.sku.isBlank()) throw new IllegalArgumentException("SKU_REQUIRED");
	    if (req.basePrice == null) throw new IllegalArgumentException("PRICE_REQUIRED");

	    if (repo.existsSlug(req.slug, excludeProductId)) throw new IllegalStateException("SLUG_EXISTS");
	    if (repo.existsSku(req.sku, excludeProductId)) throw new IllegalStateException("SKU_EXISTS");
	  }

	  private void validateVariant(VariantUpsertRequest req, Long excludeVariantId) {
	    if (req == null) throw new IllegalArgumentException("INVALID_BODY");
	    if (req.sku == null || req.sku.isBlank()) throw new IllegalArgumentException("VARIANT_SKU_REQUIRED");
	    if (req.price == null) throw new IllegalArgumentException("VARIANT_PRICE_REQUIRED");
	  }

}
