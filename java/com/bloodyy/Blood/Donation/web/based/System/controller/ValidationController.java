package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ValidationController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhoneUnique(
            @RequestParam String phone,
            @RequestParam(required = false) Long excludeUserId) {

        boolean available;
        if (excludeUserId != null) {
            available = !userRepository.existsByPhoneAndIdNot(phone, excludeUserId);
        } else {
            available = !userRepository.existsByPhone(phone);
        }

        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "Phone number is available" : "Phone number already exists"
        ));
    }
}
