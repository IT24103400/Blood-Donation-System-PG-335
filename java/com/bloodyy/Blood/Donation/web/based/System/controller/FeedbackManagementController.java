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
@RequestMapping("/donor/feedback")
public class FeedbackManagementController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public String viewMyFeedback(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        List<Feedback> userFeedbacks = feedbackService.getUserFeedbacks(user.getId());
        model.addAttribute("feedbacks", userFeedbacks);
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        model.addAttribute("user", user);

        return "donor-feedback-management";
    }

    @PostMapping("/edit/{feedbackId}")
    public String editFeedback(@PathVariable Long feedbackId,
                               @ModelAttribute FeedbackDTO feedbackDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        try {
            Feedback updatedFeedback = feedbackService.updateFeedback(feedbackId, feedbackDTO, user);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/donor/feedback";
    }

    @PostMapping("/delete/{feedbackId}")
    public String deleteFeedback(@PathVariable Long feedbackId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        try {
            boolean deleted = feedbackService.deleteFeedback(feedbackId, user);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Feedback deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete feedback.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/donor/feedback";
    }

    @GetMapping("/edit/{feedbackId}")
    public String showEditForm(@PathVariable Long feedbackId,
                               Model model,
                               HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DONOR".equals(user.getUser_type())) {
            return "redirect:/login";
        }

        Feedback feedback = feedbackService.getFeedbackByIdAndUserId(feedbackId, user.getId());
        if (feedback == null) {
            return "redirect:/donor/feedback?error=not_found";
        }

        // Only allow editing if status is PENDING
        if (!"PENDING".equals(feedback.getStatus())) {
            return "redirect:/donor/feedback?error=cannot_edit";
        }

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setContent(feedback.getContent());
        feedbackDTO.setFeedbackId(feedback.getId());

        model.addAttribute("feedback", feedback);
        model.addAttribute("feedbackDTO", feedbackDTO);
        model.addAttribute("user", user);

        return "edit-feedback";
    }
}