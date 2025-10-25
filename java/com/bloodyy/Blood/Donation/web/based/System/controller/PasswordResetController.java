// File: PasswordResetController.java
package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        boolean success = passwordResetService.requestPasswordReset(email);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "If an account with that email exists, we've sent password reset instructions.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to send reset instructions. Please try again.");
        }

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("error", "Invalid or expired reset link. Please request a new password reset.");
            return "reset-password-error";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        boolean success = passwordResetService.resetPassword(token, password);

        if (success) {
            model.addAttribute("successMessage", "Password reset successful! Check your email for the new password.");
            return "reset-password-success";
        } else {
            model.addAttribute("error", "Invalid or expired reset link. Please request a new password reset.");
            return "reset-password-error";
        }
    }
}