package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.BloodDonationCamp;
import com.bloodyy.Blood.Donation.web.based.System.entity.Donation;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    /**
     * Get all donations for a specific user
     */
    public List<Donation> getDonationsByUserId(Long userId) {
        return donationRepository.findByUserIdOrderByDonationDateDesc(userId);
    }

    /**
     * Get recent donations for a user with limit
     */
    public List<Donation> getRecentDonationsByUserId(Long userId, int limit) {
        List<Donation> allDonations = getDonationsByUserId(userId);
        return allDonations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get the last donation made by a user
     */
    public Donation getLastDonation(Long userId) {
        return donationRepository.findTopByUserIdOrderByDonationDateDesc(userId).orElse(null);
    }

    /**
     * Get total number of donations for a user
     */
    public long getTotalDonations(Long userId) {
        return donationRepository.countByUserId(userId);
    }

    /**
     * Get camp donations for a specific user
     */
    public List<Donation> getCampDonationsByUserId(Long userId) {
        return donationRepository.findCampDonationsByUserId(userId);
    }

    /**
     * Get the last camp donation made by a user
     */
    public Donation getLastCampDonation(Long userId) {
        return donationRepository.findLastCampDonationByUserId(userId).orElse(null);
    }

    /**
     * Get total number of camp donations for a user
     */
    public long getTotalCampDonations(Long userId) {
        return donationRepository.countCampDonationsByUserId(userId);
    }

    /**
     * Count camp donations since a specific date
     */
    public long countCampDonationsSinceDate(Long userId, LocalDate startDate) {
        return donationRepository.countCampDonationsSinceDate(userId, startDate);
    }

    /**
     * Get donations for a specific camp
     */
    public List<Donation> getDonationsByCampId(Long campId) {
        return donationRepository.findDonationsByCampId(campId);
    }

    /**
     * Get donations recorded by a specific volunteer
     */
    public List<Donation> getCampDonationsRecordedByVolunteer(Long volunteerId) {
        return donationRepository.findCampDonationsRecordedByVolunteer(volunteerId);
    }

    /**
     * Create a new regular donation record
     */
    public Donation createRegularDonation(User user, LocalDate donationDate) {
        Donation donation = new Donation();
        donation.setUser(user);
        donation.setDonationDate(donationDate);
        donation.setIsCampDonation(false);
        donation.setCamp(null);
        donation.setRecordedByVolunteerId(null);

        return donationRepository.save(donation);
    }

    /**
     * Create a new camp donation record
     */
    public Donation createCampDonation(User user, BloodDonationCamp camp, User volunteer) {
        Donation donation = new Donation();
        donation.setUser(user);
        donation.setDonationDate(LocalDate.now());
        donation.setIsCampDonation(true);
        donation.setCamp(camp);
        donation.setRecordedByVolunteerId(volunteer.getId());

        return donationRepository.save(donation);
    }

    /**
     * Check if user has donated in the last specified months
     */
    public boolean hasDonatedInLastMonths(Long userId, int months) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);
        long donationsInPeriod = donationRepository.countCampDonationsSinceDate(userId, cutoffDate);
        return donationsInPeriod > 0;
    }

    /**
     * Check if user is eligible for camp registration based on 6-month restriction
     */
    public boolean isEligibleForCampRegistration(Long userId) {
        Optional<Donation> lastCampDonation = donationRepository.findLastCampDonationByUserId(userId);

        if (lastCampDonation.isEmpty()) {
            return true; // No previous camp donations, eligible
        }

        LocalDate lastDonationDate = lastCampDonation.get().getDonationDate();
        LocalDate nextEligibleDate = lastDonationDate.plusMonths(6);

        return LocalDate.now().isAfter(nextEligibleDate) || LocalDate.now().equals(nextEligibleDate);
    }

    /**
     * Get the date when user becomes eligible for camp registration again
     */
    public LocalDate getNextCampEligibilityDate(Long userId) {
        Optional<Donation> lastCampDonation = donationRepository.findLastCampDonationByUserId(userId);

        if (lastCampDonation.isEmpty()) {
            return LocalDate.now(); // Eligible immediately
        }

        return lastCampDonation.get().getDonationDate().plusMonths(6);
    }

    /**
     * Get days remaining until user is eligible for camp registration
     */
    public long getDaysUntilCampEligible(Long userId) {
        LocalDate nextEligible = getNextCampEligibilityDate(userId);
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextEligible));
    }

    /**
     * Get donation statistics for a user
     */
    public DonationStatistics getDonationStatistics(Long userId) {
        long totalDonations = getTotalDonations(userId);
        long totalCampDonations = getTotalCampDonations(userId);
        long totalRegularDonations = totalDonations - totalCampDonations;
        LocalDate lastDonationDate = getLastDonationDate(userId);
        LocalDate lastCampDonationDate = getLastCampDonationDate(userId);
        boolean eligibleForCamps = isEligibleForCampRegistration(userId);
        LocalDate nextCampEligibilityDate = getNextCampEligibilityDate(userId);
        long daysUntilCampEligible = getDaysUntilCampEligible(userId);

        return new DonationStatistics(
                totalDonations,
                totalCampDonations,
                totalRegularDonations,
                lastDonationDate,
                lastCampDonationDate,
                eligibleForCamps,
                nextCampEligibilityDate,
                daysUntilCampEligible
        );
    }

    /**
     * Get the last donation date for a user
     */
    public LocalDate getLastDonationDate(Long userId) {
        Donation lastDonation = getLastDonation(userId);
        return lastDonation != null ? lastDonation.getDonationDate() : null;
    }

    /**
     * Get the last camp donation date for a user
     */
    public LocalDate getLastCampDonationDate(Long userId) {
        Donation lastCampDonation = getLastCampDonation(userId);
        return lastCampDonation != null ? lastCampDonation.getDonationDate() : null;
    }

    /**
     * Get donations within a date range
     */
    public List<Donation> getDonationsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Donation> allDonations = getDonationsByUserId(userId);
        return allDonations.stream()
                .filter(donation -> !donation.getDonationDate().isBefore(startDate) &&
                        !donation.getDonationDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Get donations by type (camp or regular)
     */
    public List<Donation> getDonationsByType(Long userId, boolean isCampDonation) {
        List<Donation> allDonations = getDonationsByUserId(userId);
        return allDonations.stream()
                .filter(donation -> donation.getIsCampDonation() == isCampDonation)
                .collect(Collectors.toList());
    }

    /**
     * Get donation frequency statistics
     */
    public DonationFrequency getDonationFrequency(Long userId) {
        List<Donation> allDonations = getDonationsByUserId(userId);

        if (allDonations.isEmpty()) {
            return new DonationFrequency(0, 0, 0, "No donations yet");
        }

        // Calculate average days between donations
        long totalDaysBetween = 0;
        int intervals = 0;

        for (int i = 0; i < allDonations.size() - 1; i++) {
            LocalDate date1 = allDonations.get(i).getDonationDate();
            LocalDate date2 = allDonations.get(i + 1).getDonationDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(date2, date1);
            totalDaysBetween += daysBetween;
            intervals++;
        }

        double averageDaysBetween = intervals > 0 ? (double) totalDaysBetween / intervals : 0;

        // Determine frequency category
        String frequencyCategory;
        if (averageDaysBetween <= 90) {
            frequencyCategory = "Frequent Donor (every 3 months or less)";
        } else if (averageDaysBetween <= 180) {
            frequencyCategory = "Regular Donor (every 6 months or less)";
        } else {
            frequencyCategory = "Occasional Donor (more than 6 months between donations)";
        }

        return new DonationFrequency(
                allDonations.size(),
                intervals,
                (long) averageDaysBetween,
                frequencyCategory
        );
    }

    /**
     * Get donation impact summary
     */
    public DonationImpact getDonationImpact(Long userId) {
        long totalDonations = getTotalDonations(userId);
        long livesPotentiallySaved = totalDonations * 3; // Each donation can save up to 3 lives
        long campDonations = getTotalCampDonations(userId);
        long regularDonations = totalDonations - campDonations;

        return new DonationImpact(
                totalDonations,
                campDonations,
                regularDonations,
                livesPotentiallySaved
        );
    }

    /**
     * Validate if a user can make a new donation
     */
    public DonationEligibility checkDonationEligibility(Long userId) {
        Donation lastDonation = getLastDonation(userId);

        if (lastDonation == null) {
            return new DonationEligibility(true, "Eligible for first donation", LocalDate.now());
        }

        LocalDate lastDonationDate = lastDonation.getDonationDate();
        LocalDate nextEligibleDate = lastDonationDate.plusDays(90); // 3 months for regular donations
        boolean isEligible = LocalDate.now().isAfter(nextEligibleDate) || LocalDate.now().equals(nextEligibleDate);

        String message;
        if (isEligible) {
            message = "Eligible to donate blood";
        } else {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextEligibleDate);
            message = String.format("Not eligible yet. Next eligible date: %s (%d days remaining)",
                    nextEligibleDate, daysRemaining);
        }

        return new DonationEligibility(isEligible, message, nextEligibleDate);
    }

    /**
     * Delete a donation record (admin function)
     */
    public boolean deleteDonation(Long donationId, User adminUser) throws Exception {
        if (!"SYSTEM_ADMIN".equals(adminUser.getUser_type())) {
            throw new Exception("Only system administrators can delete donation records");
        }

        if (!donationRepository.existsById(donationId)) {
            throw new Exception("Donation record not found");
        }

        try {
            donationRepository.deleteById(donationId);
            return true;
        } catch (Exception e) {
            throw new Exception("Failed to delete donation record: " + e.getMessage());
        }
    }

    /**
     * Update donation record (admin function)
     */
    public Donation updateDonation(Long donationId, LocalDate newDate, User adminUser) throws Exception {
        if (!"SYSTEM_ADMIN".equals(adminUser.getUser_type())) {
            throw new Exception("Only system administrators can update donation records");
        }

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new Exception("Donation record not found"));

        donation.setDonationDate(newDate);

        return donationRepository.save(donation);
    }

    /**
     * Get donation analytics for reporting
     */
    public DonationAnalytics getDonationAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Donation> allDonations = donationRepository.findAll();

        List<Donation> donationsInPeriod = allDonations.stream()
                .filter(donation -> !donation.getDonationDate().isBefore(startDate) &&
                        !donation.getDonationDate().isAfter(endDate))
                .collect(Collectors.toList());

        long totalDonations = donationsInPeriod.size();
        long campDonations = donationsInPeriod.stream()
                .filter(Donation::getIsCampDonation)
                .count();
        long regularDonations = totalDonations - campDonations;

        // Group by month for trend analysis
        Map<String, Long> monthlyTrend = donationsInPeriod.stream()
                .collect(Collectors.groupingBy(
                        donation -> donation.getDonationDate().getMonth().toString() + " " + donation.getDonationDate().getYear(),
                        Collectors.counting()
                ));

        return new DonationAnalytics(
                totalDonations,
                campDonations,
                regularDonations,
                monthlyTrend,
                startDate,
                endDate
        );
    }

    // INNER CLASSES FOR DATA TRANSFER

    public static class DonationStatistics {
        private final long totalDonations;
        private final long totalCampDonations;
        private final long totalRegularDonations;
        private final LocalDate lastDonationDate;
        private final LocalDate lastCampDonationDate;
        private final boolean eligibleForCampRegistration;
        private final LocalDate nextCampEligibilityDate;
        private final long daysUntilCampEligible;

        public DonationStatistics(long totalDonations, long totalCampDonations, long totalRegularDonations,
                                  LocalDate lastDonationDate, LocalDate lastCampDonationDate,
                                  boolean eligibleForCampRegistration, LocalDate nextCampEligibilityDate,
                                  long daysUntilCampEligible) {
            this.totalDonations = totalDonations;
            this.totalCampDonations = totalCampDonations;
            this.totalRegularDonations = totalRegularDonations;
            this.lastDonationDate = lastDonationDate;
            this.lastCampDonationDate = lastCampDonationDate;
            this.eligibleForCampRegistration = eligibleForCampRegistration;
            this.nextCampEligibilityDate = nextCampEligibilityDate;
            this.daysUntilCampEligible = daysUntilCampEligible;
        }

        // Getters
        public long getTotalDonations() { return totalDonations; }
        public long getTotalCampDonations() { return totalCampDonations; }
        public long getTotalRegularDonations() { return totalRegularDonations; }
        public LocalDate getLastDonationDate() { return lastDonationDate; }
        public LocalDate getLastCampDonationDate() { return lastCampDonationDate; }
        public boolean isEligibleForCampRegistration() { return eligibleForCampRegistration; }
        public LocalDate getNextCampEligibilityDate() { return nextCampEligibilityDate; }
        public long getDaysUntilCampEligible() { return daysUntilCampEligible; }
    }

    public static class DonationFrequency {
        private final long totalDonations;
        private final long donationIntervals;
        private final long averageDaysBetween;
        private final String frequencyCategory;

        public DonationFrequency(long totalDonations, long donationIntervals,
                                 long averageDaysBetween, String frequencyCategory) {
            this.totalDonations = totalDonations;
            this.donationIntervals = donationIntervals;
            this.averageDaysBetween = averageDaysBetween;
            this.frequencyCategory = frequencyCategory;
        }

        // Getters
        public long getTotalDonations() { return totalDonations; }
        public long getDonationIntervals() { return donationIntervals; }
        public long getAverageDaysBetween() { return averageDaysBetween; }
        public String getFrequencyCategory() { return frequencyCategory; }
    }

    public static class DonationImpact {
        private final long totalDonations;
        private final long campDonations;
        private final long regularDonations;
        private final long livesPotentiallySaved;

        public DonationImpact(long totalDonations, long campDonations,
                              long regularDonations, long livesPotentiallySaved) {
            this.totalDonations = totalDonations;
            this.campDonations = campDonations;
            this.regularDonations = regularDonations;
            this.livesPotentiallySaved = livesPotentiallySaved;
        }

        // Getters
        public long getTotalDonations() { return totalDonations; }
        public long getCampDonations() { return campDonations; }
        public long getRegularDonations() { return regularDonations; }
        public long getLivesPotentiallySaved() { return livesPotentiallySaved; }
    }

    public static class DonationEligibility {
        private final boolean eligible;
        private final String message;
        private final LocalDate nextEligibleDate;

        public DonationEligibility(boolean eligible, String message, LocalDate nextEligibleDate) {
            this.eligible = eligible;
            this.message = message;
            this.nextEligibleDate = nextEligibleDate;
        }

        // Getters
        public boolean isEligible() { return eligible; }
        public String getMessage() { return message; }
        public LocalDate getNextEligibleDate() { return nextEligibleDate; }
    }

    public static class DonationAnalytics {
        private final long totalDonations;
        private final long campDonations;
        private final long regularDonations;
        private final Map<String, Long> monthlyTrend;
        private final LocalDate startDate;
        private final LocalDate endDate;

        public DonationAnalytics(long totalDonations, long campDonations, long regularDonations,
                                 Map<String, Long> monthlyTrend, LocalDate startDate, LocalDate endDate) {
            this.totalDonations = totalDonations;
            this.campDonations = campDonations;
            this.regularDonations = regularDonations;
            this.monthlyTrend = monthlyTrend;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters
        public long getTotalDonations() { return totalDonations; }
        public long getCampDonations() { return campDonations; }
        public long getRegularDonations() { return regularDonations; }
        public Map<String, Long> getMonthlyTrend() { return monthlyTrend; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
}