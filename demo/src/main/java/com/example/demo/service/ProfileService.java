package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.MyProfileResponse;
import com.example.demo.dto.UpdateMyProfileRequest;
import com.example.demo.dto.UpsertAddressRequest;
import com.example.demo.repository.ProfileRepository;

@Service
public class ProfileService {

	private final ProfileRepository repo;

    public ProfileService(ProfileRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse me(long userId) {
        return repo.findMyProfile(userId)
            .orElseThrow(() -> new NoSuchElementException("USER_NOT_FOUND"));
    }

    @Transactional
    public MyProfileResponse updateMe(long userId, UpdateMyProfileRequest req) {
        String fullName = must(req.fullName, "fullName");

        String phone = (req.phone == null || req.phone.trim().isEmpty()) ? null : req.phone.trim();
        String avatarUrl = (req.avatarUrl == null || req.avatarUrl.trim().isEmpty()) ? null : req.avatarUrl.trim();

        try {
            repo.updateMyProfile(userId, fullName, phone, avatarUrl);
        } catch (DuplicateKeyException e) {

            throw new IllegalArgumentException("PHONE_ALREADY_USED");
        }

        return me(userId);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> listAddresses(long userId) {
        return repo.listMyAddresses(userId);
    }

    @Transactional
    public AddressDto createAddress(long userId, UpsertAddressRequest req) {
        AddressDto a = toAddressDto(req);

        if (Boolean.TRUE.equals(req.isDefault)) {
            repo.clearDefaultAddress(userId);
            a.isDefault = true;
        }

        long id = repo.insertAddress(userId, a);
        return repo.findMyAddressById(userId, id)
            .orElseThrow(() -> new IllegalStateException("ADDRESS_INSERT_FAILED"));
    }

    @Transactional
    public AddressDto updateAddress(long userId, long addressId, UpsertAddressRequest req) {
        repo.findMyAddressById(userId, addressId)
            .orElseThrow(() -> new NoSuchElementException("ADDRESS_NOT_FOUND"));

        AddressDto a = toAddressDto(req);

        if (Boolean.TRUE.equals(req.isDefault)) {
            repo.clearDefaultAddress(userId);
            a.isDefault = true;
        }

        int ok = repo.updateAddress(userId, addressId, a);
        if (ok != 1) throw new NoSuchElementException("ADDRESS_NOT_FOUND");

        return repo.findMyAddressById(userId, addressId)
            .orElseThrow(() -> new NoSuchElementException("ADDRESS_NOT_FOUND"));
    }

    @Transactional
    public void deleteAddress(long userId, long addressId) {
        int ok = repo.deleteAddress(userId, addressId);
        if (ok != 1) throw new NoSuchElementException("ADDRESS_NOT_FOUND");
    }

    private AddressDto toAddressDto(UpsertAddressRequest req) {
        AddressDto a = new AddressDto();
        a.fullName = must(req.fullName, "fullName");
        a.phone = must(req.phone, "phone");
        a.province = must(req.province, "province");
        a.district = must(req.district, "district");
        a.ward = must(req.ward, "ward");
        a.street = must(req.street, "street");
        a.isDefault = Boolean.TRUE.equals(req.isDefault);
        return a;
    }

    private String must(String s, String field) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("MISSING_" + field.toUpperCase());
        return s.trim();
    }
}
