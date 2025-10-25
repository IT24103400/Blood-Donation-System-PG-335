package com.bloodyy.Blood.Donation.web.based.System.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "hospital_staff_id")
    private User hospitalStaff;

    @ManyToOne
    @JoinColumn(name = "medical_staff_id")
    private User medicalStaff;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private String adminReply;

    @Column
    private LocalDateTime repliedAt;

    @Column
    private LocalDateTime replyUpdatedAt;

    @ManyToOne
    @JoinColumn(name = "replied_by")
    private User repliedBy;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getHospitalStaff() { return hospitalStaff; }
    public void setHospitalStaff(User hospitalStaff) { this.hospitalStaff = hospitalStaff; }

    public User getMedicalStaff() { return medicalStaff; }
    public void setMedicalStaff(User medicalStaff) { this.medicalStaff = medicalStaff; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

    public LocalDateTime getReplyUpdatedAt() { return replyUpdatedAt; }
    public void setReplyUpdatedAt(LocalDateTime replyUpdatedAt) { this.replyUpdatedAt = replyUpdatedAt; }

    public User getRepliedBy() { return repliedBy; }
    public void setRepliedBy(User repliedBy) { this.repliedBy = repliedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}