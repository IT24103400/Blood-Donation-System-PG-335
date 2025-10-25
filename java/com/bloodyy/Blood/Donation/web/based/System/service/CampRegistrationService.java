// File: CampRegistrationService.java
package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.BloodDonationCamp;
import com.bloodyy.Blood.Donation.web.based.System.entity.CampRegistration;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.BloodDonationCampRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.CampRegistrationRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampRegistrationService {

    @Autowired
    private CampRegistrationRepository campRegistrationRepository;

    @Autowired
    private BloodDonationCampRepository campRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BloodDonationCampService campService;

    @Autowired
    private DonationEligibilityService donationEligibilityService;

    public CampRegistration registerForCamp(Long campId, User donor) throws Exception {
        // Check donation eligibility first (6-month restriction)
        DonationEligibilityService.DonationEligibility donationEligibility =
                donationEligibilityService.checkDonationEligibility(donor.getId());
        if (!donationEligibility.isEligible()) {
            throw new Exception(donationEligibility.getMessage());
        }

        // Check if donor is verified
        if (!"DONOR".equals(donor.getUser_type()) || !donor.getIsVerified()) {
            throw new Exception("Only verified donors can register for blood donation camps");
        }

        // Check if camp exists and is active
        BloodDonationCamp camp = campRepository.findById(campId)
                .orElseThrow(() -> new Exception("Camp not found"));

        // Check if volunteer organizer is verified
        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("This camp is organized by an unverified volunteer");
        }

        // Check if camp is in the future
        if (camp.getCampDate().isBefore(java.time.LocalDate.now())) {
            throw new Exception("Cannot register for past camps");
        }

        // Check if donor is already registered
        if (campRegistrationRepository.existsByCampIdAndDonorIdAndStatus(campId, donor.getId(), "REGISTERED")) {
            throw new Exception("You are already registered for this camp");
        }

        // Check camp capacity
        long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
        if (currentRegistrations >= camp.getMaxDonors()) {
            throw new Exception("Camp is at full capacity");
        }

        // Create registration
        CampRegistration registration = new CampRegistration();
        registration.setCamp(camp);
        registration.setDonor(donor);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus("REGISTERED");

        // Update current donors count
        camp.setCurrentDonors((int) currentRegistrations + 1);
        campRepository.save(camp);

        return campRegistrationRepository.save(registration);
    }

    public boolean cancelRegistration(Long campId, User donor) throws Exception {
        CampRegistration registration = campRegistrationRepository
                .findByCampIdAndDonorId(campId, donor.getId())
                .orElseThrow(() -> new Exception("Registration not found"));

        if (!"REGISTERED".equals(registration.getStatus())) {
            throw new Exception("Registration is not active");
        }

        // Update registration status
        registration.setStatus("CANCELLED");
        campRegistrationRepository.save(registration);

        // Update camp current donors count
        BloodDonationCamp camp = registration.getCamp();
        long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
        camp.setCurrentDonors((int) currentRegistrations);
        campRepository.save(camp);

        return true;
    }

    public List<CampRegistration> getDonorRegistrations(Long donorId) {
        return campRegistrationRepository.findByDonorIdAndStatus(donorId, "REGISTERED");
    }

    public List<CampRegistration> getCampRegistrations(Long campId) {
        return campRegistrationRepository.findByCampIdAndStatus(campId, "REGISTERED");
    }

    public boolean isDonorRegistered(Long campId, Long donorId) {
        return campRegistrationRepository.existsByCampIdAndDonorIdAndStatus(campId, donorId, "REGISTERED");
    }

    // NEW METHODS FOR ATTENDANCE MANAGEMENT

    /**
     * Get all registered donors for a camp who haven't had attendance recorded yet
     */
    public List<User> getRegisteredDonorsWithoutAttendance(Long campId) {
        List<CampRegistration> registrations = getCampRegistrations(campId);
        return registrations.stream()
                .map(CampRegistration::getDonor)
                .filter(donor -> donor.getIsVerified())
                .toList();
    }

    /**
     * Search registered donors by name for a specific camp
     */
    public List<User> searchRegisteredDonorsByName(Long campId, String searchQuery) {
        List<CampRegistration> registrations = getCampRegistrations(campId);
        return registrations.stream()
                .map(CampRegistration::getDonor)
                .filter(donor -> donor.getIsVerified() &&
                        (donor.getFirstName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                donor.getLastName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                (donor.getFirstName() + " " + donor.getLastName()).toLowerCase().contains(searchQuery.toLowerCase())))
                .toList();
    }

    /**
     * Get registration statistics for a camp
     */
    public CampRegistrationStatistics getCampRegistrationStatistics(Long campId) {
        List<CampRegistration> registrations = getCampRegistrations(campId);
        BloodDonationCamp camp = campService.getCampById(campId);

        int totalRegistrations = registrations.size();
        int activeRegistrations = (int) registrations.stream()
                .filter(reg -> "REGISTERED".equals(reg.getStatus()))
                .count();
        int cancelledRegistrations = (int) registrations.stream()
                .filter(reg -> "CANCELLED".equals(reg.getStatus()))
                .count();

        double registrationRate = camp != null && camp.getMaxDonors() > 0 ?
                (activeRegistrations * 100.0) / camp.getMaxDonors() : 0;

        return new CampRegistrationStatistics(
                totalRegistrations,
                activeRegistrations,
                cancelledRegistrations,
                registrationRate
        );
    }

    /**
     * Check if a donor can register for a camp (with detailed validation including 6-month restriction)
     */
    public RegistrationEligibility checkRegistrationEligibility(Long campId, User donor) {
        try {
            // Check donation eligibility first (6-month restriction)
            DonationEligibilityService.DonationEligibility donationEligibility =
                    donationEligibilityService.checkDonationEligibility(donor.getId());
            if (!donationEligibility.isEligible()) {
                return new RegistrationEligibility(false, donationEligibility.getMessage());
            }

            BloodDonationCamp camp = campService.getCampById(campId);
            if (camp == null) {
                return new RegistrationEligibility(false, "Camp not found");
            }

            if (!camp.getIsActive()) {
                return new RegistrationEligibility(false, "Camp is not active");
            }

            if (!"DONOR".equals(donor.getUser_type())) {
                return new RegistrationEligibility(false, "Only donors can register for camps");
            }

            if (!donor.getIsVerified()) {
                return new RegistrationEligibility(false, "Donor is not verified");
            }

            if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
                return new RegistrationEligibility(false, "Camp organizer is not verified");
            }

            if (camp.getCampDate().isBefore(java.time.LocalDate.now())) {
                return new RegistrationEligibility(false, "Cannot register for past camps");
            }

            long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            if (currentRegistrations >= camp.getMaxDonors()) {
                return new RegistrationEligibility(false, "Camp is at full capacity");
            }

            if (campRegistrationRepository.existsByCampIdAndDonorIdAndStatus(campId, donor.getId(), "REGISTERED")) {
                return new RegistrationEligibility(false, "Already registered for this camp");
            }

            return new RegistrationEligibility(true, "Eligible to register");

        } catch (Exception e) {
            return new RegistrationEligibility(false, "Error checking eligibility: " + e.getMessage());
        }
    }

    /**
     * Get donation eligibility status for a donor
     */
    public DonationEligibilityService.DonationEligibility getDonationEligibility(Long donorId) {
        return donationEligibilityService.checkDonationEligibility(donorId);
    }

    // INNER CLASSES FOR DATA TRANSFER

    public static class CampRegistrationStatistics {
        private final int totalRegistrations;
        private final int activeRegistrations;
        private final int cancelledRegistrations;
        private final double registrationRate;

        public CampRegistrationStatistics(int totalRegistrations, int activeRegistrations,
                                          int cancelledRegistrations, double registrationRate) {
            this.totalRegistrations = totalRegistrations;
            this.activeRegistrations = activeRegistrations;
            this.cancelledRegistrations = cancelledRegistrations;
            this.registrationRate = registrationRate;
        }

        // Getters
        public int getTotalRegistrations() { return totalRegistrations; }
        public int getActiveRegistrations() { return activeRegistrations; }
        public int getCancelledRegistrations() { return cancelledRegistrations; }
        public double getRegistrationRate() { return registrationRate; }
    }

    public static class RegistrationEligibility {
        private final boolean eligible;
        private final String message;

        public RegistrationEligibility(boolean eligible, String message) {
            this.eligible = eligible;
            this.message = message;
        }

        // Getters
        public boolean isEligible() { return eligible; }
        public String getMessage() { return message; }
    }
}