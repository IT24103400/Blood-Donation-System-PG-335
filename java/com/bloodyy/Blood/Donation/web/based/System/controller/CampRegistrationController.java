// File: CampRegistrationController.java
package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.CampRegistrationService;
import com.bloodyy.Blood.Donation.web.based.System.service.BloodDonationCampService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CampRegistrationController {

    @Autowired
    private CampRegistrationService campRegistrationService;

    @Autowired
    private BloodDonationCampService campService;

    @GetMapping("/donor/camps")
    public String viewAvailableCamps(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("availableCamps", campService.getAvailableCampsForDonor(user));
        model.addAttribute("myRegistrations", campRegistrationService.getDonorRegistrations(user.getId()));

        return "donor-available-camps";
    }

    @PostMapping("/donor/camps/register/{campId}")
    public String registerForCamp(@PathVariable Long campId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        try {
            campRegistrationService.registerForCamp(campId, user);
            redirectAttributes.addFlashAttribute("success", "Successfully registered for the camp!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }

        return "redirect:/donor/camps";
    }

    @PostMapping("/donor/camps/cancel/{campId}")
    public String cancelRegistration(@PathVariable Long campId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        try {
            campRegistrationService.cancelRegistration(campId, user);
            redirectAttributes.addFlashAttribute("success", "Registration cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cancellation failed: " + e.getMessage());
        }

        return "redirect:/donor/camps";
    }

    @GetMapping("/volunteer/camps/registrations/{campId}")
    public String viewCampRegistrations(@PathVariable Long campId,
                                        Model model,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"VOLUNTEER".equals(user.getUser_type()) || !user.getIsVolunteerVerified()) {
            return "redirect:/login";
        }

        try {
            // Verify the volunteer owns this camp
            if (!campService.canEditCamp(campId, user)) {
                throw new Exception("Access denied");
            }

            model.addAttribute("user", user);
            model.addAttribute("camp", campService.getCampById(campId));
            model.addAttribute("registrations", campRegistrationService.getCampRegistrations(campId));

        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }

        return "volunteer-camp-registrations";
    }
}