package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.dto.FeedbackDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.Feedback;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback createFeedback(FeedbackDTO feedbackDTO, User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setContent(feedbackDTO.getContent());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setStatus("PENDING");

        return feedbackRepository.save(feedback);
    }

    public Feedback createMedicalStaffFeedback(FeedbackDTO feedbackDTO, User medicalStaff) {
        Feedback feedback = new Feedback();
        feedback.setUser(medicalStaff);
        feedback.setMedicalStaff(medicalStaff);
        feedback.setContent(feedbackDTO.getContent());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setStatus("PENDING");

        return feedbackRepository.save(feedback);
    }

    public Feedback createHospitalStaffFeedback(FeedbackDTO feedbackDTO, User hospitalStaff) {
        Feedback feedback = new Feedback();
        feedback.setUser(hospitalStaff);
        feedback.setHospitalStaff(hospitalStaff);
        feedback.setContent(feedbackDTO.getContent());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setStatus("PENDING");

        return feedbackRepository.save(feedback);
    }

    public Feedback updateFeedback(Long feedbackId, FeedbackDTO feedbackDTO, User user) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findByUserIdAndId(user.getId(), feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();

            // Only allow editing if status is PENDING
            if ("PENDING".equals(feedback.getStatus())) {
                feedback.setContent(feedbackDTO.getContent());
                feedback.setUpdatedAt(LocalDateTime.now());
                return feedbackRepository.save(feedback);
            } else {
                throw new IllegalArgumentException("Cannot edit feedback that has been processed");
            }
        }
        throw new IllegalArgumentException("Feedback not found or you don't have permission to edit it");
    }

    public boolean deleteFeedback(Long feedbackId, User user) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findByUserIdAndId(user.getId(), feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();

            // Only allow deletion if status is PENDING
            if ("PENDING".equals(feedback.getStatus())) {
                feedbackRepository.delete(feedback);
                return true;
            } else {
                throw new IllegalArgumentException("Cannot delete feedback that has been processed");
            }
        }
        return false;
    }

    public List<Feedback> getAllApprovedFeedbacks() {
        return feedbackRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Feedback> getUserFeedbacks(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public List<Feedback> getMedicalStaffFeedbacks(Long medicalStaffId) {
        return feedbackRepository.findByMedicalStaffId(medicalStaffId);
    }

    public List<Feedback> getHospitalStaffFeedbacks(Long hospitalStaffId) {
        return feedbackRepository.findByHospitalStaffId(hospitalStaffId);
    }

    public List<Feedback> getUserPendingFeedbacks(Long userId) {
        return feedbackRepository.findPendingByUserId(userId);
    }

    public List<Feedback> getMedicalStaffPendingFeedbacks(Long medicalStaffId) {
        return feedbackRepository.findPendingByMedicalStaffId(medicalStaffId);
    }

    public List<Feedback> getHospitalStaffPendingFeedbacks(Long hospitalStaffId) {
        return feedbackRepository.findPendingByHospitalStaffId(hospitalStaffId);
    }

    public Feedback approveFeedback(Long feedbackId, User admin) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();
            feedback.setStatus("APPROVED");
            return feedbackRepository.save(feedback);
        }
        throw new IllegalArgumentException("Feedback not found");
    }

    public Feedback rejectFeedback(Long feedbackId, User admin) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();
            feedback.setStatus("REJECTED");
            return feedbackRepository.save(feedback);
        }
        throw new IllegalArgumentException("Feedback not found");
    }

    public Feedback addReply(Long feedbackId, String reply, User admin) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();
            feedback.setAdminReply(reply);
            feedback.setRepliedAt(LocalDateTime.now());
            feedback.setRepliedBy(admin);
            return feedbackRepository.save(feedback);
        }
        throw new IllegalArgumentException("Feedback not found");
    }

    public Feedback updateReply(Long feedbackId, String reply, User admin) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();

            // Check if the admin is the one who originally replied
            if (feedback.getRepliedBy() != null && feedback.getRepliedBy().getId().equals(admin.getId())) {
                feedback.setAdminReply(reply);
                feedback.setReplyUpdatedAt(LocalDateTime.now());
                return feedbackRepository.save(feedback);
            } else {
                throw new IllegalArgumentException("You can only edit replies that you created");
            }
        }
        throw new IllegalArgumentException("Feedback not found");
    }

    public void deleteFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }

    public long getPendingFeedbackCount() {
        return feedbackRepository.countByStatus("PENDING");
    }

    public Feedback getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId).orElse(null);
    }

    public Feedback getFeedbackByIdAndUserId(Long feedbackId, Long userId) {
        return feedbackRepository.findByUserIdAndId(userId, feedbackId).orElse(null);
    }

    public List<Feedback> getAllMedicalStaffFeedbacks() {
        return feedbackRepository.findAllMedicalStaffFeedbacks();
    }

    public List<Feedback> getAllHospitalStaffFeedbacks() {
        return feedbackRepository.findAllHospitalStaffFeedbacks();
    }

    public long countMedicalStaffFeedbacks() {
        return feedbackRepository.countByMedicalStaffIsNotNull();
    }

    public long countHospitalStaffFeedbacks() {
        return feedbackRepository.countByHospitalStaffIsNotNull();
    }

    public List<Feedback> getRecentFeedbacks(int limit) {
        return feedbackRepository.findTopNByOrderByCreatedAtDesc(limit);
    }

    public List<Feedback> getFeedbacksByStatus(String status) {
        return feedbackRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Feedback> getFeedbacksWithAdminReplies() {
        return feedbackRepository.findByAdminReplyIsNotNullOrderByRepliedAtDesc();
    }

    public List<Feedback> getPendingFeedbacks() {
        return feedbackRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }
}