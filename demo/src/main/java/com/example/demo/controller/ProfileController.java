
package com.example.demo.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.MyProfileResponse;
import com.example.demo.dto.UpdateMyProfileRequest;
import com.example.demo.dto.UpsertAddressRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ProfileService;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    private long currentUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"
            );
        }
        return principal.getUser().getUserId();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bad(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("message", ex.getMessage());
    }

    // ===== profile =====

    @GetMapping
    public MyProfileResponse me(Authentication auth) {
        return service.me(currentUserId(auth));
    }

    @PutMapping
    public MyProfileResponse updateMe(Authentication auth, @RequestBody UpdateMyProfileRequest req) {
        return service.updateMe(currentUserId(auth), req);
    }

    @GetMapping("/addresses")
    public java.util.List<AddressDto> listAddresses(Authentication auth) {
        return service.listAddresses(currentUserId(auth));
    }

    @PostMapping("/addresses")
    public AddressDto createAddress(Authentication auth, @RequestBody UpsertAddressRequest req) {
        return service.createAddress(currentUserId(auth), req);
    }

    @PutMapping("/addresses/{addressId}")
    public AddressDto updateAddress(Authentication auth, @PathVariable long addressId, @RequestBody UpsertAddressRequest req) {
        return service.updateAddress(currentUserId(auth), addressId, req);
    }

    @DeleteMapping("/addresses/{addressId}")
    public Map<String, Object> deleteAddress(Authentication auth, @PathVariable long addressId) {
        service.deleteAddress(currentUserId(auth), addressId);
        return Map.of("ok", true);
    }
}
