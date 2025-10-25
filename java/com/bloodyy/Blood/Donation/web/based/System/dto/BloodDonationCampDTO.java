package com.bloodyy.Blood.Donation.web.based.System.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BloodDonationCampDTO {
    private Long id;
    private String campName;
    private String description;
    private String location;
    private LocalDate campDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxDonors;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getCampDate() { return campDate; }
    public void setCampDate(LocalDate campDate) { this.campDate = campDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getMaxDonors() { return maxDonors; }
    public void setMaxDonors(Integer maxDonors) { this.maxDonors = maxDonors; }
}