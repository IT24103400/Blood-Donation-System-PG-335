package com.bloodyy.Blood.Donation.web.based.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Mock data for demonstration; replace with actual service calls to fetch data
        model.addAttribute("registeredDonors", 1234);
        model.addAttribute("requestsFulfilled", 567);
        model.addAttribute("livesSaved", 1890);

        // Blood type availability data
        model.addAttribute("bloodTypes", new String[][] {
                {"A+", "URGENT"},
                {"B-", "NEEDED"},
                {"O-", "URGENT"},
                {"AB+", "AVAILABLE"},
                {"A-", "NEEDED"},
                {"B+", "URGENT"},
                {"AB-", "AVAILABLE"},
                {"O+", "NEEDED"}
        });

        return "index"; // Maps to index.html in templates folder
    }
}