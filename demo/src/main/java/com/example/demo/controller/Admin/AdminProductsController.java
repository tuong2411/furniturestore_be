package com.example.demo.controller.Admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.admin.AdminProductUpsertRequest;
import com.example.demo.dto.admin.ProductImageRequest;
import com.example.demo.dto.admin.UpdateActiveRequest;
import com.example.demo.dto.admin.VariantUpsertRequest;
import com.example.demo.service.AdminProductService;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminProductsController {
	private final AdminProductService service;

	  public AdminProductsController(AdminProductService service) {
	    this.service = service;
	  }

	  // LIST + search + filter + paging
	  @GetMapping
	  public ResponseEntity<?> list(
	      @RequestParam(required = false) String keyword,
	      @RequestParam(required = false) Long categoryId,
	      @RequestParam(required = false) Boolean active,
	      @RequestParam(defaultValue = "0") int page,
	      @RequestParam(defaultValue = "10") int size
	  ) {
	    return ResponseEntity.ok(service.list(keyword, categoryId, active, page, size));
	  }

	  // DETAIL trả product + images + variants + styles
	  @GetMapping("/{id}")
	  public ResponseEntity<?> detail(@PathVariable("id") long id) {
	    return ResponseEntity.ok(service.detail(id));
	  }

	  // CREATE
	  @PostMapping
	  public ResponseEntity<?> create(@RequestBody AdminProductUpsertRequest req) {
	    return ResponseEntity.ok(service.create(req));
	  }

	  // UPDATE
	  @PutMapping("/{id}")
	  public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody AdminProductUpsertRequest req) {
	    return ResponseEntity.ok(service.update(id, req));
	  }

	  // SOFT ACTIVE/INACTIVE
	  @PatchMapping("/{id}/active")
	  public ResponseEntity<?> active(@PathVariable("id") long id, @RequestBody UpdateActiveRequest req) {
	    boolean isActive = req != null && Boolean.TRUE.equals(req.isActive);
	    return ResponseEntity.ok(service.updateActive(id, isActive));
	  }

	  // ===== Images =====

	  @GetMapping("/{id}/images")
	  public ResponseEntity<?> images(@PathVariable("id") long id) {
	    return ResponseEntity.ok(service.detail(id).get("images"));
	  }

	  @PostMapping("/{id}/images")
	  public ResponseEntity<?> addImage(@PathVariable("id") long id, @RequestBody ProductImageRequest req) {
	    return ResponseEntity.ok(service.addImage(id, req));
	  }

	  @PatchMapping("/{id}/images/{imageId}/main")
	  public ResponseEntity<?> setMain(@PathVariable("id") long id, @PathVariable("imageId") long imageId) {
	    return ResponseEntity.ok(service.setMainImage(id, imageId));
	  }

	  @DeleteMapping("/{id}/images/{imageId}")
	  public ResponseEntity<?> deleteImage(@PathVariable("id") long id, @PathVariable("imageId") long imageId) {
	    return ResponseEntity.ok(service.deleteImage(id, imageId));
	  }

	  // ===== Variants =====

	  @GetMapping("/{id}/variants")
	  public ResponseEntity<?> variants(@PathVariable("id") long id) {
	    return ResponseEntity.ok(service.detail(id).get("variants"));
	  }

	  @PostMapping("/{id}/variants")
	  public ResponseEntity<?> addVariant(@PathVariable("id") long id, @RequestBody VariantUpsertRequest req) {
	    return ResponseEntity.ok(service.addVariant(id, req));
	  }

	  @PutMapping("/variants/{variantId}")
	  public ResponseEntity<?> updateVariant(@PathVariable("variantId") long variantId, @RequestBody VariantUpsertRequest req) {
	    return ResponseEntity.ok(service.updateVariant(variantId, req));
	  }

	  @PatchMapping("/variants/{variantId}/active")
	  public ResponseEntity<?> variantActive(@PathVariable("variantId") long variantId, @RequestBody UpdateActiveRequest req) {
	    boolean isActive = req != null && Boolean.TRUE.equals(req.isActive);
	    return ResponseEntity.ok(service.updateVariantActive(variantId, isActive));
	  }

}
