package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.*;
import com.bloodyy.Blood.Donation.web.based.System.repository.CampAttendanceRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.CampRegistrationRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampAttendanceService {

    @Autowired
    private CampAttendanceRepository campAttendanceRepository;

    @Autowired
    private CampRegistrationRepository campRegistrationRepository;

    @Autowired
    private BloodDonationCampService campService;

    @Autowired
    private UserService userService;

    @Autowired
    private DonationRepository donationRepository;

    /**
     * Record attendance for a donor in any camp (any verified volunteer can do this)
     */
    public CampAttendance recordAttendance(Long campId, Long donorId, User volunteer) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can record attendance");
        }

        // Verify camp exists and has verified organizer
        BloodDonationCamp camp = campService.getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        // Check if camp organizer is verified (ANY verified volunteer can record attendance)
        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        // Verify donor is registered for this camp
        if (!campRegistrationRepository.existsByCampIdAndDonorIdAndStatus(campId, donorId, "REGISTERED")) {
            throw new Exception("Donor is not registered for this camp");
        }

        // Check if attendance already recorded
        if (campAttendanceRepository.existsByCampIdAndDonorId(campId, donorId)) {
            throw new Exception("Attendance already recorded for this donor");
        }

        // Get donor details
        User donor = userService.getUserById(donorId);
        if (donor == null || !"DONOR".equals(donor.getUser_type()) || !donor.getIsVerified()) {
            throw new Exception("Invalid or unverified donor");
        }

        // Record attendance
        CampAttendance attendance = new CampAttendance();
        attendance.setCamp(camp);
        attendance.setDonor(donor);
        attendance.setRecordedBy(volunteer);
        attendance.setAttendedAt(LocalDateTime.now());
        attendance.setBloodDonated(false);
        attendance.setNotes("");

        return campAttendanceRepository.save(attendance);
    }

    /**
     * Mark blood donation for any attendee and create donation record
     */
    public CampAttendance markBloodDonation(Long campId, Long donorId, User volunteer, String notes) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can record blood donations");
        }

        // Get attendance record
        CampAttendance attendance = campAttendanceRepository.findByCampIdAndDonorId(campId, donorId)
                .orElseThrow(() -> new Exception("Attendance record not found"));

        // ANY verified volunteer can mark blood donation (removed ownership check)
        BloodDonationCamp camp = attendance.getCamp();
        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        // Update blood donation status
        attendance.setBloodDonated(true);
        attendance.setNotes(notes != null ? notes : "");

        // Create donation record for 6-month restriction
        createDonationRecord(donorId, camp, volunteer);

        return campAttendanceRepository.save(attendance);
    }

    /**
     * Create a donation record when blood donation is marked
     */
    private void createDonationRecord(Long donorId, BloodDonationCamp camp, User volunteer) {
        try {
            Donation donation = new Donation();
            donation.setUser(userService.getUserById(donorId));
            donation.setDonationDate(java.time.LocalDate.now());
            donation.setCamp(camp);
            donation.setIsCampDonation(true);
            donation.setRecordedByVolunteerId(volunteer.getId());

            donationRepository.save(donation);
        } catch (Exception e) {
            System.err.println("Error creating donation record: " + e.getMessage());
            // Don't fail the attendance marking if donation record creation fails
        }
    }

    /**
     * Get camp attendees (any verified volunteer can view)
     */
    public List<CampAttendance> getCampAttendees(Long campId, User volunteer) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can view attendance");
        }

        // Verify camp exists and has verified organizer
        BloodDonationCamp camp = campService.getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        return campAttendanceRepository.findByCampIdOrderByAttendedAtDesc(campId);
    }

    /**
     * Search attendees by name (any verified volunteer can search)
     */
    public List<CampAttendance> searchAttendees(Long campId, String searchQuery, User volunteer) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can search attendance");
        }

        // Verify camp exists and has verified organizer
        BloodDonationCamp camp = campService.getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return getCampAttendees(campId, volunteer);
        }

        return campAttendanceRepository.searchAttendeesByName(campId, searchQuery.trim());
    }

    /**
     * Get attendee details (any verified volunteer can view)
     */
    public CampAttendance getAttendeeDetails(Long campId, Long donorId, User volunteer) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can view attendee details");
        }

        // Verify camp exists and has verified organizer
        BloodDonationCamp camp = campService.getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        return campAttendanceRepository.findByCampIdAndDonorId(campId, donorId)
                .orElseThrow(() -> new Exception("Attendee not found"));
    }

    /**
     * Get camp statistics (any verified volunteer can view)
     */
    public CampStatistics getCampStatistics(Long campId, User volunteer) throws Exception {
        // Verify volunteer is verified
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can view statistics");
        }

        // Verify camp exists and has verified organizer
        BloodDonationCamp camp = campService.getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        List<CampAttendance> attendees = campAttendanceRepository.findByCampIdOrderByAttendedAtDesc(campId);
        long bloodDonations = campAttendanceRepository.countBloodDonationsByCamp(campId);
        long totalAttendees = campAttendanceRepository.countByCampId(campId);

        return new CampStatistics(
                (int) totalAttendees,
                (int) bloodDonations,
                camp.getMaxDonors() != null ? camp.getMaxDonors() : 0,
                camp.getCurrentDonors() != null ? camp.getCurrentDonors() : 0
        );
    }

    /**
     * Get global attendance statistics across all camps
     */
    public GlobalAttendanceStatistics getGlobalAttendanceStatistics(User volunteer) throws Exception {
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can view global statistics");
        }

        List<BloodDonationCamp> allVerifiedCamps = campService.getAllActiveVerifiedCamps();

        int totalCamps = allVerifiedCamps.size();
        int totalAttendees = 0;
        int totalBloodDonations = 0;
        int totalRegisteredDonors = 0;

        for (BloodDonationCamp camp : allVerifiedCamps) {
            totalAttendees += campAttendanceRepository.countByCampId(camp.getId());
            totalBloodDonations += campAttendanceRepository.countBloodDonationsByCamp(camp.getId());
            totalRegisteredDonors += camp.getCurrentDonors() != null ? camp.getCurrentDonors() : 0;
        }

        return new GlobalAttendanceStatistics(
                totalCamps,
                totalAttendees,
                totalBloodDonations,
                totalRegisteredDonors
        );
    }

    /**
     * Check if a volunteer can manage attendance for a specific camp
     */
    public boolean canManageAttendance(Long campId, User volunteer) {
        try {
            BloodDonationCamp camp = campService.getCampById(campId);
            return camp != null &&
                    camp.getIsActive() &&
                    camp.getOrganizedBy().getIsVolunteerVerified() &&
                    volunteer.getIsVolunteerVerified() &&
                    "VOLUNTEER".equals(volunteer.getUser_type());
        } catch (Exception e) {
            return false;
        }
    }

    // INNER CLASSES
    public static class CampStatistics {
        private final int totalAttendees;
        private final int bloodDonations;
        private final int maxCapacity;
        private final int registeredDonors;

        public CampStatistics(int totalAttendees, int bloodDonations, int maxCapacity, int registeredDonors) {
            this.totalAttendees = totalAttendees;
            this.bloodDonations = bloodDonations;
            this.maxCapacity = maxCapacity;
            this.registeredDonors = registeredDonors;
        }

        public int getTotalAttendees() { return totalAttendees; }
        public int getBloodDonations() { return bloodDonations; }
        public int getMaxCapacity() { return maxCapacity; }
        public int getRegisteredDonors() { return registeredDonors; }
        public double getDonationRate() {
            return totalAttendees > 0 ? (bloodDonations * 100.0) / totalAttendees : 0;
        }
    }

    public static class GlobalAttendanceStatistics {
        private final int totalCamps;
        private final int totalAttendees;
        private final int totalBloodDonations;
        private final int totalRegisteredDonors;

        public GlobalAttendanceStatistics(int totalCamps, int totalAttendees, int totalBloodDonations, int totalRegisteredDonors) {
            this.totalCamps = totalCamps;
            this.totalAttendees = totalAttendees;
            this.totalBloodDonations = totalBloodDonations;
            this.totalRegisteredDonors = totalRegisteredDonors;
        }

        public int getTotalCamps() { return totalCamps; }
        public int getTotalAttendees() { return totalAttendees; }
        public int getTotalBloodDonations() { return totalBloodDonations; }
        public int getTotalRegisteredDonors() { return totalRegisteredDonors; }
        public double getGlobalDonationRate() {
            return totalAttendees > 0 ? (totalBloodDonations * 100.0) / totalAttendees : 0;
        }
    }
}