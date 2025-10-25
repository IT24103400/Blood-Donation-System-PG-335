package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.dto.FeedbackDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.Donation;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private DonorDashboardService donorDashboardService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @Autowired
    private DonationService donationService;

    @GetMapping("/dashboard")
    public String donorDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        long totalDonations = donorDashboardService.getTotalDonations(user.getId());
        long totalCampDonations = donorDashboardService.getTotalCampDonations(user.getId());
        long pendingFeedbackCount = feedbackService.getUserPendingFeedbacks(user.getId()).size();

        // Get urgent blood requests for this donor
        List<com.bloodyy.Blood.Donation.web.based.System.entity.Notification> urgentBloodRequests = donorDashboardService.getUrgentBloodRequests(user.getId());
        long urgentRequestCount = donorDashboardService.getUrgentRequestCount(user.getId());

        // Get camp registration eligibility information (6-month restriction)
        boolean canRegisterForCamps = donorDashboardService.isEligibleForCampRegistration(user.getId());
        DonorDashboardService.DonorStatistics donorStats = donorDashboardService.getDonorStatistics(user.getId());
        DonationEligibilityService.DonationEligibility donationEligibility = donorDashboardService.getCampDonationEligibility(user.getId());

        // Get donation history for dashboard
        List<Donation> recentDonations = donationService.getDonationsByUserId(user.getId()).stream()
                .limit(10)
                .collect(Collectors.toList());

        // Get camp donation history
        List<Donation> campDonationHistory = donorDashboardService.getCampDonationHistory(user.getId());
        Donation lastCampDonation = donorDashboardService.getLastCampDonation(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("totalCampDonations", totalCampDonations);
        model.addAttribute("lastDonationDate", donorDashboardService.getLastDonationDate(user.getId()));
        model.addAttribute("lastCampDonationDate", donorDashboardService.getLastCampDonationDate(user.getId()));
        model.addAttribute("nextEligibleDate", donorDashboardService.getNextEligibleDate(user.getId()));
        model.addAttribute("bloodType", user.getBloodType());
        model.addAttribute("isEligible", donorDashboardService.isEligibleNow(user.getId()));
        model.addAttribute("daysToNext", donorDashboardService.getDaysToNext(user.getId()));
        model.addAttribute("notifications", donorDashboardService.getRecentNotifications(user.getId()));
        model.addAttribute("donorRanking", 247);
        model.addAttribute("pendingFeedbackCount", pendingFeedbackCount);
        model.addAttribute("urgentBloodRequests", urgentBloodRequests);
        model.addAttribute("urgentRequestCount", urgentRequestCount);

        // Add camp registration eligibility information
        model.addAttribute("canRegisterForCamps", canRegisterForCamps);
        model.addAttribute("nextCampEligibilityDate", donorStats.getNextCampEligibilityDate());
        model.addAttribute("daysUntilCampEligible", donorStats.getDaysUntilCampEligible());
        model.addAttribute("donationEligibility", donationEligibility);
        model.addAttribute("campDonationHistory", campDonationHistory);
        model.addAttribute("lastCampDonation", lastCampDonation);
        model.addAttribute("hasDonatedInLastSixMonths", donorDashboardService.hasDonatedInLastSixMonths(user.getId()));
        model.addAttribute("donorStats", donorStats);

        // Add donation history for dashboard display
        model.addAttribute("donations", recentDonations);

        // Add feedback DTO and user's feedback history
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        model.addAttribute("userFeedbacks", feedbackService.getUserFeedbacks(user.getId()));

        return "donor-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?success=logout";
    }

    @GetMapping("/donor/profile")
    public String viewProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        // Add camp eligibility information to profile page
        DonorDashboardService.DonorStatistics donorStats = donorDashboardService.getDonorStatistics(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("isEditing", false);
        model.addAttribute("donorStats", donorStats);
        model.addAttribute("canRegisterForCamps", donorStats.isEligibleForCampRegistration());

        return "donor-profile";
    }

    @GetMapping("/donor/profile/edit")
    public String editProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("isEditing", true);
        return "donor-profile";
    }

    @PostMapping("/donor/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam(required = false) String bloodType,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        try {
            User updated;

            // Use the new method if blood type is being set for the first time
            if (user.getBloodType() == null && bloodType != null && !bloodType.trim().isEmpty()) {
                updated = userService.updateUserProfileWithBloodType(
                        user.getId(),
                        updatedUser.getFirstName(),
                        updatedUser.getLastName(),
                        updatedUser.getPhone(),
                        updatedUser.getAddress(),
                        bloodType
                );
            } else {
                // Use existing method for regular profile updates
                updated = userService.updateUserProfile(
                        user.getId(),
                        updatedUser.getFirstName(),
                        updatedUser.getLastName(),
                        updatedUser.getPhone(),
                        updatedUser.getAddress()
                );
            }

            if (updated != null) {
                // Update session with new user data
                session.setAttribute("user", updated);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update profile");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/donor/profile";
    }

    @GetMapping("/donor/history")
    public String donationHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        // Get donation history
        List<Donation> donations = donationService.getDonationsByUserId(user.getId());

        // Get camp donation history specifically
        List<Donation> campDonations = donorDashboardService.getCampDonationHistory(user.getId());

        // Get eligibility information
        DonorDashboardService.DonorStatistics donorStats = donorDashboardService.getDonorStatistics(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("donations", donations);
        model.addAttribute("campDonations", campDonations);
        model.addAttribute("totalDonations", donations.size());
        model.addAttribute("totalCampDonations", campDonations.size());
        model.addAttribute("donorStats", donorStats);
        model.addAttribute("canRegisterForCamps", donorStats.isEligibleForCampRegistration());

        return "donor-history";
    }

    @GetMapping("/donor/delete-account")
    public String showDeleteAccountPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "donor-delete-account";
    }

    @PostMapping("/donor/delete-account/confirm")
    public String deleteAccount(@RequestParam String confirmPassword,
                                @RequestParam(required = false) String reason,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        // Verify password
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Incorrect password. Account deletion cancelled.");
            return "redirect:/donor/delete-account";
        }

        try {
            // Optional: Log the deletion reason
            if (reason != null && !reason.trim().isEmpty()) {
                System.out.println("Account deletion reason for user " + user.getEmail() + ": " + reason);
                // You could save this to a separate table for analysis
            }

            // Delete the account
            boolean deleted = userService.deleteDonorAccount(user.getId(), user);

            if (deleted) {
                // Invalidate session
                session.invalidate();
                redirectAttributes.addFlashAttribute("success",
                        "Your account has been successfully deleted. We're sorry to see you go!");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete account. Please try again.");
                return "redirect:/donor/delete-account";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/donor/delete-account";
        }
    }
}