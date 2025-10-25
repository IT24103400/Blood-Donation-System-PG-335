package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.entity.EmailChangeRequest;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.EmailChangeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class EmailChangeController {

    @Autowired
    private EmailChangeService emailChangeService;

    @GetMapping("/email-change-requests")
    public String viewEmailChangeRequests(@RequestParam(required = false) String search,
                                          Model model, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        List<EmailChangeRequest> requests;
        if (search != null && !search.trim().isEmpty()) {
            requests = emailChangeService.searchPendingRequests(search);
        } else {
            requests = emailChangeService.getPendingRequests();
        }

        model.addAttribute("admin", admin);
        model.addAttribute("requests", requests);
        model.addAttribute("searchQuery", search);
        model.addAttribute("totalPending", requests.size());

        return "admin-email-change-requests";
    }

    @PostMapping("/process-email-change/{requestId}")
    @ResponseBody
    public ResponseEntity<?> processEmailChange(@PathVariable Long requestId,
                                                HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized access"
            ));
        }

        try {
            boolean success = emailChangeService.processEmailChange(requestId, admin.getId());

            if (success) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Email change processed successfully! New credentials sent to user."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to process email change request. User may not exist or request already processed."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/cancel-email-change/{requestId}")
    @ResponseBody
    public ResponseEntity<?> cancelEmailChangeRequest(@PathVariable Long requestId,
                                                      @RequestBody Map<String, String> requestData,
                                                      HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized access"
            ));
        }

        try {
            String cancellationReason = requestData.get("cancellationReason");

            boolean success = emailChangeService.cancelEmailChangeRequest(requestId, admin.getId(), cancellationReason);

            if (success) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Email change request cancelled successfully! User has been notified."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to cancel email change request. Request may not exist or already processed."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
}