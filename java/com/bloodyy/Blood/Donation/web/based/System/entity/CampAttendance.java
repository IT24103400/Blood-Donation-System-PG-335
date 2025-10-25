// File: CampAttendance.java
package com.bloodyy.Blood.Donation.web.based.System.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "camp_attendance")
public class CampAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camp_id", nullable = false)
    private BloodDonationCamp camp;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy; // Volunteer who recorded the attendance

    @Column(nullable = false)
    private LocalDateTime attendedAt;

    @Column(nullable = false)
    private Boolean bloodDonated = false;

    @Column
    private String notes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BloodDonationCamp getCamp() { return camp; }
    public void setCamp(BloodDonationCamp camp) { this.camp = camp; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getAttendedAt() { return attendedAt; }
    public void setAttendedAt(LocalDateTime attendedAt) { this.attendedAt = attendedAt; }

    public Boolean getBloodDonated() { return bloodDonated; }
    public void setBloodDonated(Boolean bloodDonated) { this.bloodDonated = bloodDonated; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}