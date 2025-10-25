package com.bloodyy.Blood.Donation.web.based.System.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String user_type;
    // "DONOR", "VOLUNTEER", "HOSPITAL_STAFF", "MEDICAL_STAFF",
    // "REGIONAL_LIAISON", "BLOOD_BANK_ADMIN", "SYSTEM_ADMIN"

    @Column
    private String bloodType; // Only for donors

    @Column
    private LocalDate dateOfBirth;

    @Column(unique = true)  // Add unique constraint
    private String phone;

    @Column
    private String address;

    @Column(name = "is_temp_password", nullable = false)
    private Boolean isTempPassword = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin ID who verified

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // In User.java entity, add these fields:
    @Column(name = "is_volunteer_verified", nullable = false)
    private Boolean isVolunteerVerified = false;

    @Column(name = "volunteer_verified_by")
    private Long volunteerVerifiedBy; // Admin ID who verified volunteer

    @Column(name = "volunteer_verified_at")
    private LocalDateTime volunteerVerifiedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsTempPassword() {
        return isTempPassword;
    }

    public void setIsTempPassword(Boolean isTempPassword) {
        this.isTempPassword = isTempPassword;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }


    public Boolean getIsVolunteerVerified() {
        return isVolunteerVerified;
    }

    public void setIsVolunteerVerified(Boolean isVolunteerVerified) {
        this.isVolunteerVerified = isVolunteerVerified;
    }

    public Long getVolunteerVerifiedBy() {
        return volunteerVerifiedBy;
    }

    public void setVolunteerVerifiedBy(Long volunteerVerifiedBy) {
        this.volunteerVerifiedBy = volunteerVerifiedBy;
    }

    public LocalDateTime getVolunteerVerifiedAt() {
        return volunteerVerifiedAt;
    }

    public void setVolunteerVerifiedAt(LocalDateTime volunteerVerifiedAt) {
        this.volunteerVerifiedAt = volunteerVerifiedAt;
    }
}
