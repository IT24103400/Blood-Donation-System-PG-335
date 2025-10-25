package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/admin/test-email")
    @ResponseBody
    public String testEmail(@RequestParam String email) {
        try {
            emailService.sendWelcomeEmail(
                    email,
                    "Test",
                    "User",
                    "MEDICAL_STAFF",
                    "testPassword123"
            );
            return "Test email sent successfully to: " + email;
        } catch (Exception e) {
            return "Failed to send test email: " + e.getMessage();
        }
    }

    @GetMapping("/admin/email-config")
    @ResponseBody
    public String checkEmailConfig() {
        return "Email service is " + (emailService != null ? "available" : "not available");
    }
}