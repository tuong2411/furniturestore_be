package com.example.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VnpayService {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public String createPaymentUrl(long orderId, long amount, String ipAddr) {
        Map<String, String> vnp = new HashMap<>();
        vnp.put("vnp_Version", "2.1.0");
        vnp.put("vnp_Command", "pay");
        vnp.put("vnp_TmnCode", tmnCode);
        vnp.put("vnp_Amount", String.valueOf(amount)); 
        vnp.put("vnp_CurrCode", "VND");
        vnp.put("vnp_TxnRef", String.valueOf(orderId));
        vnp.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
        vnp.put("vnp_OrderType", "other");
        vnp.put("vnp_Locale", "vn");
        vnp.put("vnp_ReturnUrl", returnUrl);
        vnp.put("vnp_IpAddr", ipAddr);
        vnp.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> keys = new ArrayList<>(vnp.keySet());
        Collections.sort(keys);

        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();

        for (String k : keys) {
            String val = vnp.get(k);
            if (val == null || val.isBlank()) continue;

            String encVal = enc(val);

            query.append(k).append("=").append(encVal).append("&");
            hashData.append(k).append("=").append(encVal).append("&");
        }

        query.setLength(query.length() - 1);
        hashData.setLength(hashData.length() - 1);

        String secureHash = HmacSHA512.hmac(hashSecret, hashData.toString());
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }
}
