package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.entity.EmailChangeRequest;
import com.bloodyy.Blood.Donation.web.based.System.service.EmailChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class PublicEmailChangeController {

    @Autowired
    private EmailChangeService emailChangeService;

    @PostMapping("/api/email-change-request")
    @ResponseBody
    public ResponseEntity<?> submitEmailChangeRequest(@RequestBody Map<String, String> requestData) {
        try {
            String currentEmail = requestData.get("currentEmail");
            String newEmail = requestData.get("newEmail");
            String fullName = requestData.get("fullName");
            String reason = requestData.get("reason");

            // Validate required fields
            if (currentEmail == null || currentEmail.trim().isEmpty() ||
                    newEmail == null || newEmail.trim().isEmpty() ||
                    fullName == null || fullName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "All required fields must be filled"
                ));
            }

            // Check if emails are different
            if (currentEmail.equals(newEmail)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "New email must be different from current email"
                ));
            }

            // Submit the request
            EmailChangeRequest savedRequest = emailChangeService.submitEmailChangeRequest(
                    currentEmail, newEmail, fullName, reason
            );

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Email change request submitted successfully! Our admin team will process it within 24 hours.",
                    "requestId", savedRequest.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error submitting request: " + e.getMessage()
            ));
        }
    }
}