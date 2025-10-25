package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.dto.UserCreationDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.Donation;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.UserService;
import com.bloodyy.Blood.Donation.web.based.System.service.DonationService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private DonationService donationService;

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SYSTEM_ADMIN".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        // Get all staff users
        List<User> staffUsers = userService.getStaffUsers();
        model.addAttribute("staffUsers", staffUsers);
        model.addAttribute("totalStaff", staffUsers.size());

        // Get all donors
        List<User> donors = userService.getUsersByType("DONOR");
        model.addAttribute("donors", donors);
        model.addAttribute("totalDonors", donors.size());

        // Get all volunteers
        List<User> volunteers = userService.getUsersByType("VOLUNTEER");
        model.addAttribute("volunteers", volunteers);
        model.addAttribute("totalVolunteers", volunteers.size());

        // Add user creation DTO for the form
        model.addAttribute("userCreationDTO", new UserCreationDTO());

        return "admin-dashboard";
    }

    // Create User
    @PostMapping("/create-user")
    public String createUser(@ModelAttribute UserCreationDTO userCreationDTO,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            // Validate phone uniqueness before creation
            if (userCreationDTO.getPhone() != null &&
                    !userCreationDTO.getPhone().trim().isEmpty()) {
                // Additional validation can be added here if needed
            }

            User createdUser = userService.createUser(userCreationDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User created successfully! Credentials have been sent to: " + createdUser.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    // Generate Temporary Password
    @GetMapping("/generate-password")
    @ResponseBody
    public String generatePassword() {
        return userService.generateTemporaryPassword();
    }

    // Delete User
    @PostMapping("/delete-user/{userId}")
    public String deleteUser(@PathVariable Long userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    // Manage All Users
    @GetMapping("/users")
    public String manageUsers(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SYSTEM_ADMIN".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("userCreationDTO", new UserCreationDTO());

        return "admin-users";
    }

    // Resend Credentials
    @PostMapping("/resend-credentials/{userId}")
    public String resendCredentials(@PathVariable Long userId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            User user = userService.getAllUsers().stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new Exception("User not found"));

            String tempPassword = userService.generateTemporaryPassword();

            // Update user password
            user.setPassword(tempPassword);
            userService.updateUser(user);

            // Resend credentials
            userService.getEmailService().sendWelcomeEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUser_type(),
                    tempPassword
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Credentials resent to: " + user.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error resending credentials: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Manage Donors
    @GetMapping("/donors")
    public String manageDonors(@RequestParam(required = false) String search,
                               Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SYSTEM_ADMIN".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        List<User> donors;
        if (search != null && !search.trim().isEmpty()) {
            donors = userService.searchDonorsByName(search);
        } else {
            donors = userService.getUsersByType("DONOR");
        }

        model.addAttribute("user", user);
        model.addAttribute("donors", donors);
        model.addAttribute("searchQuery", search);
        model.addAttribute("totalDonors", donors.size());

        return "admin-donors";
    }

    // Verify Donor
    @PostMapping("/verify-donor/{donorId}")
    @ResponseBody
    public ResponseEntity<?> verifyDonor(@PathVariable Long donorId,
                                         HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User verifiedDonor = userService.verifyDonor(donorId, admin.getId());
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Donor verified successfully!",
                    "donorName", verifiedDonor.getFirstName() + " " + verifiedDonor.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // Unverify Donor
    @PostMapping("/unverify-donor/{donorId}")
    @ResponseBody
    public ResponseEntity<?> unverifyDonor(@PathVariable Long donorId,
                                           HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User unverifiedDonor = userService.unverifyDonor(donorId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Donor verification removed!",
                    "donorName", unverifiedDonor.getFirstName() + " " + unverifiedDonor.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // ✅ New Methods for Viewing Donor Details

    @GetMapping("/donor/{donorId}")
    public String viewDonorDetails(@PathVariable Long donorId, Model model, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            User donor = userService.getUserById(donorId);
            if (donor == null || !"DONOR".equals(donor.getUser_type())) {
                throw new Exception("Donor not found");
            }

            // Get donation history for this donor
            List<Donation> donations = donationService.getDonationsByUserId(donorId);
            long totalDonations = donations.size();

            // Get last donation date
            LocalDate lastDonationDate = null;
            if (!donations.isEmpty()) {
                lastDonationDate = donations.get(0).getDonationDate();
            }

            model.addAttribute("admin", admin);
            model.addAttribute("donor", donor);
            model.addAttribute("donations", donations);
            model.addAttribute("totalDonations", totalDonations);
            model.addAttribute("lastDonationDate", lastDonationDate);
            model.addAttribute("livesSaved", totalDonations * 3);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading donor details: " + e.getMessage());
            return "redirect:/admin/donors";
        }

        return "admin-donor-details";
    }

    @PostMapping("/donor/{donorId}/verify")
    @ResponseBody
    public ResponseEntity<?> verifyDonorDetail(@PathVariable Long donorId, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User verifiedDonor = userService.verifyDonor(donorId, admin.getId());
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Donor verified successfully!",
                    "donorName", verifiedDonor.getFirstName() + " " + verifiedDonor.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/donor/{donorId}/unverify")
    @ResponseBody
    public ResponseEntity<?> unverifyDonorDetail(@PathVariable Long donorId, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User unverifiedDonor = userService.unverifyDonor(donorId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Donor verification removed!",
                    "donorName", unverifiedDonor.getFirstName() + " " + unverifiedDonor.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // Manage Volunteers
    @GetMapping("/volunteers")
    public String manageVolunteers(@RequestParam(required = false) String search,
                                   Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SYSTEM_ADMIN".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        List<User> volunteers;
        if (search != null && !search.trim().isEmpty()) {
            volunteers = userService.searchVolunteersByName(search);
        } else {
            volunteers = userService.getUsersByType("VOLUNTEER");
        }

        model.addAttribute("user", user);
        model.addAttribute("volunteers", volunteers);
        model.addAttribute("searchQuery", search);
        model.addAttribute("totalVolunteers", volunteers.size());
        model.addAttribute("verifiedVolunteersCount", getVerifiedVolunteersCount());
        model.addAttribute("pendingVerificationCount", getPendingVolunteerVerificationCount());

        return "admin-volunteers";
    }

    // Verify Volunteer
    @PostMapping("/verify-volunteer/{volunteerId}")
    @ResponseBody
    public ResponseEntity<?> verifyVolunteer(@PathVariable Long volunteerId,
                                             HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User verifiedVolunteer = userService.verifyVolunteer(volunteerId, admin.getId());
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Volunteer verified successfully!",
                    "volunteerName", verifiedVolunteer.getFirstName() + " " + verifiedVolunteer.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // Unverify Volunteer
    @PostMapping("/unverify-volunteer/{volunteerId}")
    @ResponseBody
    public ResponseEntity<?> unverifyVolunteer(@PathVariable Long volunteerId,
                                               HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User unverifiedVolunteer = userService.unverifyVolunteer(volunteerId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Volunteer verification removed!",
                    "volunteerName", unverifiedVolunteer.getFirstName() + " " + unverifiedVolunteer.getLastName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    // View Volunteer Details
    @GetMapping("/volunteer/{volunteerId}")
    public String viewVolunteerDetails(@PathVariable Long volunteerId, Model model, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            User volunteer = userService.getUserById(volunteerId);
            if (volunteer == null || !"VOLUNTEER".equals(volunteer.getUser_type())) {
                throw new Exception("Volunteer not found");
            }

            // Mock data for volunteer statistics
            long completedTasks = 45; // Replace with actual service call
            long pendingTasks = 12;   // Replace with actual service call
            long hoursContributed = 156; // Replace with actual service call

            model.addAttribute("admin", admin);
            model.addAttribute("volunteer", volunteer);
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("hoursContributed", hoursContributed);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading volunteer details: " + e.getMessage());
            return "redirect:/admin/volunteers";
        }

        return "admin-volunteer-details";
    }

    // In AdminDashboardController.java, add these helper methods:

    private long getVerifiedVolunteersCount() {
        return userService.getUsersByType("VOLUNTEER").stream()
                .filter(User::getIsVolunteerVerified)
                .count();
    }

    private long getPendingVolunteerVerificationCount() {
        return userService.getUsersByType("VOLUNTEER").stream()
                .filter(volunteer -> !volunteer.getIsVolunteerVerified())
                .count();
    }
}
