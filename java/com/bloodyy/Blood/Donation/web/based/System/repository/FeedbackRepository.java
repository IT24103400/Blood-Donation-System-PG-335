package com.bloodyy.Blood.Donation.web.based.System.repository;

import com.bloodyy.Blood.Donation.web.based.System.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Basic CRUD operations are inherited from JpaRepository

    // Find feedback by status, ordered by creation date (descending)
    List<Feedback> findByStatusOrderByCreatedAtDesc(String status);

    // Find all feedback, ordered by creation date (descending)
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // Find feedback by user ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserId(@Param("userId") Long userId);

    // Find specific feedback by user ID and feedback ID
    @Query("SELECT f FROM Feedback f WHERE f.user.id = :userId AND f.id = :feedbackId")
    Optional<Feedback> findByUserIdAndId(@Param("userId") Long userId, @Param("feedbackId") Long feedbackId);

    // Find feedback by hospital staff ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.hospitalStaff.id = :hospitalStaffId ORDER BY f.createdAt DESC")
    List<Feedback> findByHospitalStaffId(@Param("hospitalStaffId") Long hospitalStaffId);

    // Find feedback by medical staff ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.medicalStaff.id = :medicalStaffId ORDER BY f.createdAt DESC")
    List<Feedback> findByMedicalStaffId(@Param("medicalStaffId") Long medicalStaffId);

    // Find pending feedback by hospital staff ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.hospitalStaff.id = :hospitalStaffId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
    List<Feedback> findPendingByHospitalStaffId(@Param("hospitalStaffId") Long hospitalStaffId);

    // Find pending feedback by medical staff ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.medicalStaff.id = :medicalStaffId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
    List<Feedback> findPendingByMedicalStaffId(@Param("medicalStaffId") Long medicalStaffId);

    // Count feedback by status
    long countByStatus(String status);

    // Find pending feedback by user ID, ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.user.id = :userId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
    List<Feedback> findPendingByUserId(@Param("userId") Long userId);

    // Find all medical staff feedbacks (where medicalStaff is not null)
    @Query("SELECT f FROM Feedback f WHERE f.medicalStaff IS NOT NULL ORDER BY f.createdAt DESC")
    List<Feedback> findAllMedicalStaffFeedbacks();

    // Find all hospital staff feedbacks (where hospitalStaff is not null)
    @Query("SELECT f FROM Feedback f WHERE f.hospitalStaff IS NOT NULL ORDER BY f.createdAt DESC")
    List<Feedback> findAllHospitalStaffFeedbacks();

    // Count feedbacks where medicalStaff is not null
    long countByMedicalStaffIsNotNull();

    // Count feedbacks where hospitalStaff is not null
    long countByHospitalStaffIsNotNull();

    // Find top N feedbacks ordered by creation date (descending)
    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC LIMIT :limit")
    List<Feedback> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

    // Find feedbacks with admin replies, ordered by reply date (descending)
    @Query("SELECT f FROM Feedback f WHERE f.adminReply IS NOT NULL ORDER BY f.repliedAt DESC")
    List<Feedback> findByAdminReplyIsNotNullOrderByRepliedAtDesc();

    // Find feedbacks by status
    List<Feedback> findByStatus(String status);

    // Find feedbacks created after a specific date
    @Query("SELECT f FROM Feedback f WHERE f.createdAt >= :date ORDER BY f.createdAt DESC")
    List<Feedback> findByCreatedAtAfter(@Param("date") java.time.LocalDateTime date);

    // Find feedbacks created between two dates
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate ORDER BY f.createdAt DESC")
    List<Feedback> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                          @Param("endDate") java.time.LocalDateTime endDate);

    // Find feedbacks by user type
    @Query("SELECT f FROM Feedback f WHERE f.user.user_type = :userType ORDER BY f.createdAt DESC")
    List<Feedback> findByUserType(@Param("userType") String userType);

    // Find feedbacks by blood type (for donors)
    @Query("SELECT f FROM Feedback f WHERE f.user.bloodType = :bloodType ORDER BY f.createdAt DESC")
    List<Feedback> findByUserBloodType(@Param("bloodType") String bloodType);

    // Find feedbacks that need admin attention (pending for more than 7 days)
    @Query("SELECT f FROM Feedback f WHERE f.status = 'PENDING' AND f.createdAt <= :thresholdDate ORDER BY f.createdAt ASC")
    List<Feedback> findOldPendingFeedbacks(@Param("thresholdDate") java.time.LocalDateTime thresholdDate);

    // Find feedbacks with specific content (search functionality)
    @Query("SELECT f FROM Feedback f WHERE LOWER(f.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY f.createdAt DESC")
    List<Feedback> findByContentContaining(@Param("searchTerm") String searchTerm);

    // Find feedbacks by admin who replied
    @Query("SELECT f FROM Feedback f WHERE f.repliedBy.id = :adminId ORDER BY f.repliedAt DESC")
    List<Feedback> findByRepliedBy(@Param("adminId") Long adminId);

    // Find feedbacks that have been updated
    @Query("SELECT f FROM Feedback f WHERE f.updatedAt IS NOT NULL ORDER BY f.updatedAt DESC")
    List<Feedback> findUpdatedFeedbacks();

    // Find feedbacks with replies that have been updated
    @Query("SELECT f FROM Feedback f WHERE f.replyUpdatedAt IS NOT NULL ORDER BY f.replyUpdatedAt DESC")
    List<Feedback> findFeedbackWithUpdatedReplies();
}