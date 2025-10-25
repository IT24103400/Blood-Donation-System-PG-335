package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.dto.UserRegistrationDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDTO registrationDTO, Model model) {
        try {
            // Debug: Log the received data
            System.out.println("Registration Data Debug:");
            System.out.println("User Type: " + registrationDTO.getUser_type());
            System.out.println("Blood Type: " + registrationDTO.getBloodType());
            System.out.println("First Name: " + registrationDTO.getFirstName());
            System.out.println("Email: " + registrationDTO.getEmail());
            System.out.println("Phone: " + registrationDTO.getPhone()); // Add phone logging

            userService.registerUser(registrationDTO);
            return "redirect:/login?success";
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            // Re-populate the form data on error
            model.addAttribute("user", registrationDTO);
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "success", required = false) String success,
                                @RequestParam(value = "error", required = false) String error,
                                Model model) {
        if (success != null) {
            model.addAttribute("successMessage", "Registration successful! Please login to continue.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password,
                            @RequestParam(required = false) String user_type,
                            Model model, HttpSession session) {
        if (user_type == null || user_type.isEmpty()) {
            model.addAttribute("error", "Please select a role");
            return "login";
        }

        User user = userService.loginUser(email, password);
        if (user != null && user.getUser_type().equals(user_type)) {
            session.setAttribute("user", user);

            // Redirect based on user type
            switch (user.getUser_type()) {
                case "SYSTEM_ADMIN":
                    return "redirect:/admin/dashboard";
                case "DONOR":
                    return "redirect:/dashboard";
                case "VOLUNTEER":
                    return "redirect:/volunteer/dashboard";
                case "HOSPITAL_STAFF":
                    return "redirect:/hospital-staff/dashboard";
                case "MEDICAL_STAFF":
                    return "redirect:/medical-staff/dashboard";
                case "REGIONAL_LIAISON":
                    return "redirect:/regional-liaison/dashboard";
                default:
                    model.addAttribute("error", "Dashboard for " + user_type + " is not yet implemented.");
                    return "login";
            }
        } else {
            model.addAttribute("error", user == null ? "Invalid email or password" : "Selected role does not match your account type");
            return "login";
        }
    }
}