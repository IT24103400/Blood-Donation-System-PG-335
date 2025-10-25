// File: CampRegistration.java
package com.bloodyy.Blood.Donation.web.based.System.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "camp_registrations")
public class CampRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camp_id", nullable = false)
    private BloodDonationCamp camp;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private String status; // "REGISTERED", "CANCELLED", "ATTENDED"

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BloodDonationCamp getCamp() { return camp; }
    public void setCamp(BloodDonationCamp camp) { this.camp = camp; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}