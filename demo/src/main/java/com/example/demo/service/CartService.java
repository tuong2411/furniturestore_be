package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.CartDto;
import com.example.demo.repository.CartRepository;

@Service
public class CartService {
	private final CartRepository repo;

    public CartService(CartRepository repo) {
        this.repo = repo;
    }

    public CartDto getMyCart(long userId) {
        long cartId = repo.findActiveCartId(userId).orElseGet(() -> repo.createCart(userId));

        CartDto dto = new CartDto();
        dto.cartId = cartId;
        dto.items = new ArrayList<>();

        List<Map<String, Object>> rows = repo.findCartItems(cartId);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map<String, Object> r : rows) {
            CartDto.CartItemDto item = new CartDto.CartItemDto();
            item.cartItemId = ((Number) r.get("cart_item_id")).longValue();
            item.productId  = ((Number) r.get("product_id")).longValue();
            item.name       = (String) r.get("name");
            item.slug       = (String) r.get("slug");
            item.image      = (String) r.get("main_image");

            item.variantId  = (r.get("variant_id") == null) ? null : ((Number) r.get("variant_id")).longValue();
            item.material   = (String) r.get("material");
            item.color      = (String) r.get("color");
            item.size       = (String) r.get("size");

            item.unitPrice  = (BigDecimal) r.get("unit_price");
            item.quantity   = ((Number) r.get("quantity")).intValue();

            item.lineTotal  = item.unitPrice.multiply(BigDecimal.valueOf(item.quantity));
            subtotal = subtotal.add(item.lineTotal);

            dto.items.add(item);
        }

        dto.subtotal = subtotal;
        dto.shippingFee = BigDecimal.ZERO; 
        dto.total = dto.subtotal.add(dto.shippingFee);

        return dto;
    }

    public CartDto addToCart(long userId, AddToCartRequest req) {
        if (req.quantity <= 0) req.quantity = 1;

        long cartId = repo.findActiveCartId(userId).orElseGet(() -> repo.createCart(userId));

        Map<String, Object> priceRow = repo.getProductPrice(req.productId, req.variantId)
            .orElseThrow(() -> new NoSuchElementException("PRODUCT_NOT_FOUND"));

        // check product active
        Object isActiveObj = priceRow.get("is_active");
        int isActive = (isActiveObj instanceof Boolean b) ? (b ? 1 : 0) : ((Number) isActiveObj).intValue();
        if (isActive != 1) throw new NoSuchElementException("PRODUCT_INACTIVE");


        if (req.variantId != null) {
            if (priceRow.get("variant_id") == null) throw new NoSuchElementException("VARIANT_NOT_FOUND");

            Object vActiveObj = priceRow.get("variant_active");
            int vActive = (vActiveObj instanceof Boolean b) ? (b ? 1 : 0) : ((Number) vActiveObj).intValue();
            if (vActive != 1) throw new NoSuchElementException("VARIANT_INACTIVE");
        }

        BigDecimal basePrice = (BigDecimal) priceRow.get("base_price");
        BigDecimal variantPrice = (BigDecimal) priceRow.get("variant_price");

        BigDecimal unitPrice = (req.variantId != null && variantPrice != null) ? variantPrice : basePrice;

        // upsert
        repo.findExistingCartItemId(cartId, req.productId, req.variantId)
            .ifPresentOrElse(
                cartItemId -> repo.increaseQuantity(cartItemId, req.quantity),
                () -> repo.insertCartItem(cartId, req.productId, req.variantId, req.quantity, unitPrice)
            );

        return getMyCart(userId);
    }

    public CartDto updateQty(long userId, long cartItemId, int qty) {
        if (!repo.cartItemBelongsToUser(cartItemId, userId)) {
            throw new SecurityException("FORBIDDEN");
        }

        if (qty < 1) qty = 1;

        repo.updateQuantity(cartItemId, qty);
        return getMyCart(userId);
    }

    public CartDto removeItem(long userId, long cartItemId) {
        if (!repo.cartItemBelongsToUser(cartItemId, userId)) {
            throw new SecurityException("FORBIDDEN");
        }

        repo.deleteItem(cartItemId);
        return getMyCart(userId);
    }

    

}
