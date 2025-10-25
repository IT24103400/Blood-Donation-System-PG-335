package com.bloodyy.Blood.Donation.web.based.System.controller;

import com.bloodyy.Blood.Donation.web.based.System.dto.FeedbackDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.Feedback;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // Public endpoint to view approved feedbacks
    @GetMapping("/feedback")
    public String viewFeedback(Model model, HttpSession session) {
        List<Feedback> approvedFeedbacks = feedbackService.getAllApprovedFeedbacks();
        model.addAttribute("feedbacks", approvedFeedbacks);
        model.addAttribute("feedbackDTO", new FeedbackDTO());

        // Add user object for session check
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        return "feedback-public";
    }

    // Submit feedback (handles both public and dashboard submissions)
    @PostMapping("/submit-feedback")
    public String submitFeedback(@ModelAttribute FeedbackDTO feedbackDTO,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(value = "redirectTo", defaultValue = "feedback") String redirectTo) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to submit feedback.");
            return "redirect:/login";
        }

        // Allow donors, hospital staff, and medical staff to submit feedback
        if (!"DONOR".equals(user.getUser_type()) &&
                !"HOSPITAL_STAFF".equals(user.getUser_type()) &&
                !"MEDICAL_STAFF".equals(user.getUser_type())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your role does not have permission to submit feedback.");
            return "redirect:/" + redirectTo;
        }

        try {
            Feedback feedback = feedbackService.createFeedback(feedbackDTO, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thank you for your feedback! It will be reviewed by our team.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect to appropriate page based on source
        return "redirect:/" + redirectTo;
    }

    // Admin feedback management
    @GetMapping("/admin/feedback")
    public String manageFeedback(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"SYSTEM_ADMIN".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();
        long pendingCount = feedbackService.getPendingFeedbackCount();

        model.addAttribute("feedbacks", allFeedbacks);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("user", user);
        model.addAttribute("feedbackDTO", new FeedbackDTO());

        return "admin-feedback";
    }

    // Approve feedback
    @PostMapping("/admin/feedback/approve/{feedbackId}")
    public String approveFeedback(@PathVariable Long feedbackId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            Feedback approvedFeedback = feedbackService.approveFeedback(feedbackId, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Feedback #" + feedbackId + " approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error approving feedback: " + e.getMessage());
        }

        return "redirect:/admin/feedback";
    }

    // Reject feedback
    @PostMapping("/admin/feedback/reject/{feedbackId}")
    public String rejectFeedback(@PathVariable Long feedbackId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            Feedback rejectedFeedback = feedbackService.rejectFeedback(feedbackId, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Feedback #" + feedbackId + " rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error rejecting feedback: " + e.getMessage());
        }

        return "redirect:/admin/feedback";
    }

    // Add reply to feedback
    @PostMapping("/admin/feedback/reply/{feedbackId}")
    public String addReply(@PathVariable Long feedbackId,
                           @RequestParam String reply,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            Feedback feedback = feedbackService.addReply(feedbackId, reply, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reply added successfully to feedback #" + feedbackId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error adding reply: " + e.getMessage());
        }

        return "redirect:/admin/feedback";
    }

    // Update reply to feedback
    @PostMapping("/admin/feedback/update-reply/{feedbackId}")
    public String updateReply(@PathVariable Long feedbackId,
                              @RequestParam("reply") String reply,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            Feedback feedback = feedbackService.updateReply(feedbackId, reply, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reply updated successfully for feedback #" + feedbackId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating reply: " + e.getMessage());
        }

        return "redirect:/admin/feedback";
    }

    // Delete feedback
    @PostMapping("/admin/feedback/delete/{feedbackId}")
    public String deleteFeedback(@PathVariable Long feedbackId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        try {
            feedbackService.deleteFeedback(feedbackId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Feedback #" + feedbackId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting feedback: " + e.getMessage());
        }

        return "redirect:/admin/feedback";
    }

    // Get user's feedback history (for AJAX or API calls)
    @GetMapping("/api/feedback/user")
    @ResponseBody
    public List<Feedback> getUserFeedback(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return List.of();
        }
        return feedbackService.getUserFeedbacks(user.getId());
    }

    // Get pending feedback count (for admin dashboard)
    @GetMapping("/api/feedback/pending-count")
    @ResponseBody
    public long getPendingFeedbackCount() {
        return feedbackService.getPendingFeedbackCount();
    }

    // View single feedback details
    @GetMapping("/feedback/{feedbackId}")
    public String viewFeedbackDetails(@PathVariable Long feedbackId,
                                      Model model,
                                      HttpSession session) {
        User user = (User) session.getAttribute("user");
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);

        if (feedback == null) {
            return "redirect:/feedback?error=not_found";
        }

        // Only show approved feedback to public, or user's own feedback, or admin
        if (!"APPROVED".equals(feedback.getStatus()) &&
                (user == null ||
                        (!user.getId().equals(feedback.getUser().getId()) &&
                                !"SYSTEM_ADMIN".equals(user.getUser_type())))) {
            return "redirect:/feedback?error=access_denied";
        }

        model.addAttribute("feedback", feedback);
        model.addAttribute("user", user);

        return "feedback-details";
    }

    // Show edit reply form for admin
    @GetMapping("/admin/feedback/edit-reply/{feedbackId}")
    public String showEditReplyForm(@PathVariable Long feedbackId,
                                    Model model,
                                    HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"SYSTEM_ADMIN".equals(admin.getUser_type())) {
            return "redirect:/login";
        }

        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        if (feedback == null) {
            return "redirect:/admin/feedback?error=not_found";
        }

        // Check if the admin is the one who originally replied
        if (feedback.getRepliedBy() == null || !feedback.getRepliedBy().getId().equals(admin.getId())) {
            return "redirect:/admin/feedback?error=not_authorized";
        }

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setContent(feedback.getAdminReply());
        feedbackDTO.setFeedbackId(feedback.getId());

        model.addAttribute("feedback", feedback);
        model.addAttribute("feedbackDTO", feedbackDTO);
        model.addAttribute("user", admin);

        return "admin-edit-reply";
    }
}