// File: DonationEligibilityService.java
package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.Donation;
import com.bloodyy.Blood.Donation.web.based.System.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class DonationEligibilityService {

    @Autowired
    private DonationRepository donationRepository;

    private static final int DONATION_COOLDOWN_MONTHS = 6;

    /**
     * Check if a donor is eligible to register for new camps based on recent donations
     */
    public boolean isDonorEligibleForNewRegistration(Long donorId) {
        Optional<Donation> lastCampDonation = donationRepository.findLastCampDonationByUserId(donorId);

        if (lastCampDonation.isEmpty()) {
            return true; // No previous camp donations, eligible
        }

        LocalDate lastDonationDate = lastCampDonation.get().getDonationDate();
        LocalDate nextEligibleDate = lastDonationDate.plusMonths(DONATION_COOLDOWN_MONTHS);

        return LocalDate.now().isAfter(nextEligibleDate) || LocalDate.now().equals(nextEligibleDate);
    }

    /**
     * Get the date when donor becomes eligible again
     */
    public LocalDate getNextEligibilityDate(Long donorId) {
        Optional<Donation> lastCampDonation = donationRepository.findLastCampDonationByUserId(donorId);

        if (lastCampDonation.isEmpty()) {
            return LocalDate.now(); // Eligible immediately
        }

        return lastCampDonation.get().getDonationDate().plusMonths(DONATION_COOLDOWN_MONTHS);
    }

    /**
     * Get days remaining until donor is eligible
     */
    public long getDaysUntilEligible(Long donorId) {
        LocalDate nextEligible = getNextEligibilityDate(donorId);
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextEligible));
    }

    /**
     * Check eligibility with detailed response
     */
    public DonationEligibility checkDonationEligibility(Long donorId) {
        boolean isEligible = isDonorEligibleForNewRegistration(donorId);
        LocalDate nextEligibleDate = getNextEligibilityDate(donorId);
        long daysRemaining = getDaysUntilEligible(donorId);

        String message;
        if (isEligible) {
            message = "Eligible to register for blood donation camps";
        } else {
            message = String.format("Not eligible to register for new camps due to recent blood donation. Next eligible date: %s (%d days remaining)",
                    nextEligibleDate, daysRemaining);
        }

        return new DonationEligibility(isEligible, message, nextEligibleDate, daysRemaining);
    }

    // Inner class for detailed eligibility response
    public static class DonationEligibility {
        private final boolean eligible;
        private final String message;
        private final LocalDate nextEligibleDate;
        private final long daysRemaining;

        public DonationEligibility(boolean eligible, String message, LocalDate nextEligibleDate, long daysRemaining) {
            this.eligible = eligible;
            this.message = message;
            this.nextEligibleDate = nextEligibleDate;
            this.daysRemaining = daysRemaining;
        }

        // Getters
        public boolean isEligible() { return eligible; }
        public String getMessage() { return message; }
        public LocalDate getNextEligibleDate() { return nextEligibleDate; }
        public long getDaysRemaining() { return daysRemaining; }
    }
}