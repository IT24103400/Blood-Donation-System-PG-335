package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.BloodDonationCamp;
import com.bloodyy.Blood.Donation.web.based.System.entity.CampRegistration;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.BloodDonationCampRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.CampRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class BloodDonationCampService {

    @Autowired
    private BloodDonationCampRepository campRepository;

    @Autowired
    private CampRegistrationRepository campRegistrationRepository;

    public BloodDonationCamp createCamp(BloodDonationCamp camp, User volunteer) throws Exception {
        if (!"VOLUNTEER".equals(volunteer.getUser_type()) || !volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can organize blood donation camps");
        }

        // Validate camp date
        if (camp.getCampDate().isBefore(LocalDate.now())) {
            throw new Exception("Cannot create camp with past date");
        }

        // Validate time
        if (camp.getStartTime().isAfter(camp.getEndTime())) {
            throw new Exception("Start time cannot be after end time");
        }

        camp.setOrganizedBy(volunteer);
        camp.setCreatedAt(LocalDateTime.now());
        camp.setUpdatedAt(LocalDateTime.now());
        camp.setIsActive(true);
        camp.setCurrentDonors(0);

        return campRepository.save(camp);
    }

    public BloodDonationCamp getCampById(Long campId) {
        Optional<BloodDonationCamp> camp = campRepository.findById(campId);
        return camp.orElse(null);
    }

    public List<BloodDonationCamp> getCampsByVolunteer(Long volunteerId) {
        return campRepository.findByOrganizedByIdAndIsActiveOrderByCampDateDesc(volunteerId, true);
    }

    public List<BloodDonationCamp> getAllActiveCamps() {
        return campRepository.findByIsActiveOrderByCampDateDesc(true);
    }

    public List<BloodDonationCamp> getUpcomingCamps() {
        return campRepository.findByCampDateAfterAndIsActiveOrderByCampDate(LocalDate.now(), true);
    }

    public List<BloodDonationCamp> getTodaysCamps() {
        List<BloodDonationCamp> allActiveCamps = getAllActiveCamps();
        return allActiveCamps.stream()
                .filter(camp -> camp.getCampDate() != null && camp.getCampDate().equals(LocalDate.now()))
                .sorted((c1, c2) -> {
                    if (c1.getStartTime() == null || c2.getStartTime() == null) return 0;
                    return c1.getStartTime().compareTo(c2.getStartTime());
                })
                .collect(Collectors.toList());
    }

    public BloodDonationCamp updateCamp(Long campId, BloodDonationCamp updatedCamp, User volunteer) throws Exception {
        BloodDonationCamp existingCamp = campRepository.findById(campId)
                .orElseThrow(() -> new Exception("Camp not found"));

        if (!existingCamp.getOrganizedBy().getId().equals(volunteer.getId())) {
            throw new Exception("You can only update camps organized by you");
        }

        if (updatedCamp.getCampDate().isBefore(LocalDate.now())) {
            throw new Exception("Cannot set camp date in the past");
        }

        if (updatedCamp.getStartTime().isAfter(updatedCamp.getEndTime())) {
            throw new Exception("Start time cannot be after end time");
        }

        existingCamp.setCampName(updatedCamp.getCampName());
        existingCamp.setDescription(updatedCamp.getDescription());
        existingCamp.setLocation(updatedCamp.getLocation());
        existingCamp.setCampDate(updatedCamp.getCampDate());
        existingCamp.setStartTime(updatedCamp.getStartTime());
        existingCamp.setEndTime(updatedCamp.getEndTime());
        existingCamp.setMaxDonors(updatedCamp.getMaxDonors());
        existingCamp.setUpdatedAt(LocalDateTime.now());

        return campRepository.save(existingCamp);
    }

    public boolean deleteCamp(Long campId, User volunteer) throws Exception {
        BloodDonationCamp camp = campRepository.findById(campId)
                .orElseThrow(() -> new Exception("Camp not found"));

        if (!camp.getOrganizedBy().getId().equals(volunteer.getId())) {
            throw new Exception("You can only delete camps organized by you");
        }

        camp.setIsActive(false);
        camp.setUpdatedAt(LocalDateTime.now());
        campRepository.save(camp);

        return true;
    }

    public long getCampCountByVolunteer(Long volunteerId) {
        return campRepository.countActiveCampsByVolunteer(volunteerId);
    }

    public boolean canEditCamp(Long campId, User volunteer) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            return camp != null && camp.getOrganizedBy().getId().equals(volunteer.getId());
        } catch (Exception e) {
            return false;
        }
    }

    // =============================================
    // METHODS FOR ANY VERIFIED VOLUNTEER ACCESS
    // =============================================

    /**
     * Get all active camps organized by verified volunteers (for any volunteer to view)
     */
    public List<BloodDonationCamp> getAllActiveVerifiedCamps() {
        List<BloodDonationCamp> allActiveCamps = getAllActiveCamps();
        return allActiveCamps.stream()
                .filter(camp -> camp.getOrganizedBy().getIsVolunteerVerified())
                .collect(Collectors.toList());
    }

    /**
     * Get camps that any verified volunteer can manage attendance for
     */
    public List<BloodDonationCamp> getAttendanceEligibleCamps() {
        List<BloodDonationCamp> allActiveCamps = getAllActiveCamps();
        return allActiveCamps.stream()
                .filter(camp -> camp.getOrganizedBy().getIsVolunteerVerified() &&
                        (camp.getCampDate().isAfter(LocalDate.now()) ||
                                camp.getCampDate().equals(LocalDate.now())))
                .sorted((c1, c2) -> {
                    // Sort by date (today's first), then by start time
                    if (c1.getCampDate().equals(LocalDate.now()) && !c2.getCampDate().equals(LocalDate.now())) {
                        return -1;
                    } else if (!c1.getCampDate().equals(LocalDate.now()) && c2.getCampDate().equals(LocalDate.now())) {
                        return 1;
                    } else {
                        return c1.getCampDate().compareTo(c2.getCampDate());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get today's camps for quick attendance access
     */
    public List<BloodDonationCamp> getTodaysVerifiedCamps() {
        List<BloodDonationCamp> todaysCamps = getTodaysCamps();
        return todaysCamps.stream()
                .filter(camp -> camp.getOrganizedBy().getIsVolunteerVerified())
                .sorted((c1, c2) -> {
                    if (c1.getStartTime() == null || c2.getStartTime() == null) return 0;
                    return c1.getStartTime().compareTo(c2.getStartTime());
                })
                .collect(Collectors.toList());
    }

    /**
     * Search camps by name for attendance management
     */
    public List<BloodDonationCamp> searchCampsForAttendance(String searchQuery) {
        List<BloodDonationCamp> eligibleCamps = getAttendanceEligibleCamps();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return eligibleCamps;
        }

        String searchTerm = searchQuery.toLowerCase().trim();
        return eligibleCamps.stream()
                .filter(camp -> camp.getCampName().toLowerCase().contains(searchTerm) ||
                        camp.getLocation().toLowerCase().contains(searchTerm) ||
                        camp.getOrganizedBy().getFirstName().toLowerCase().contains(searchTerm) ||
                        camp.getOrganizedBy().getLastName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Verify if a camp is eligible for attendance management by any verified volunteer
     */
    public boolean isCampEligibleForAttendance(Long campId) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            return camp != null &&
                    camp.getIsActive() &&
                    camp.getOrganizedBy().getIsVolunteerVerified() &&
                    (camp.getCampDate().isAfter(LocalDate.now()) ||
                            camp.getCampDate().equals(LocalDate.now()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get detailed camp information for attendance management
     */
    public Map<String, Object> getCampDetailsForAttendance(Long campId) {
        Map<String, Object> campDetails = new HashMap<>();
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null || !camp.getIsActive() || !camp.getOrganizedBy().getIsVolunteerVerified()) {
                return campDetails;
            }

            campDetails.put("camp", camp);

            // Get registration statistics
            long registeredCount = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            campDetails.put("registeredCount", registeredCount);

            // Calculate attendance rate
            double attendanceRate = registeredCount > 0 ? (camp.getCurrentDonors() * 100.0) / registeredCount : 0;
            campDetails.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);

            // Get available slots
            int availableSlots = camp.getMaxDonors() - (int) registeredCount;
            campDetails.put("availableSlots", Math.max(0, availableSlots));

            // Camp status
            String status;
            if (camp.getCampDate().isBefore(LocalDate.now())) {
                status = "COMPLETED";
            } else if (camp.getCampDate().equals(LocalDate.now())) {
                status = "TODAY";
            } else {
                status = "UPCOMING";
            }
            campDetails.put("status", status);

            // Urgency level
            String urgency = "LOW";
            if (camp.getCampDate().equals(LocalDate.now())) {
                if (attendanceRate < 30) {
                    urgency = "HIGH";
                } else if (attendanceRate < 60) {
                    urgency = "MEDIUM";
                }
            }
            campDetails.put("urgency", urgency);

            return campDetails;

        } catch (Exception e) {
            return campDetails;
        }
    }

    /**
     * Get camps with registration statistics for volunteer dashboard
     */
    public List<Map<String, Object>> getCampsWithStatistics() {
        List<BloodDonationCamp> eligibleCamps = getAttendanceEligibleCamps();
        List<Map<String, Object>> campsWithStats = new ArrayList<>();

        for (BloodDonationCamp camp : eligibleCamps) {
            Map<String, Object> campStats = getCampDetailsForAttendance(camp.getId());
            campsWithStats.add(campStats);
        }

        return campsWithStats;
    }

    /**
     * Get urgent camps (today's camps with low attendance)
     */
    public List<Map<String, Object>> getUrgentCamps() {
        List<BloodDonationCamp> todaysCamps = getTodaysVerifiedCamps();
        List<Map<String, Object>> urgentCamps = new ArrayList<>();

        for (BloodDonationCamp camp : todaysCamps) {
            Map<String, Object> campDetails = getCampDetailsForAttendance(camp.getId());
            double attendanceRate = (double) campDetails.get("attendanceRate");
            long registeredCount = (long) campDetails.get("registeredCount");

            // Consider camps urgent if they have registrations but low attendance rate
            if (registeredCount > 0 && attendanceRate < 50.0) {
                campDetails.put("urgency", "HIGH");
                urgentCamps.add(campDetails);
            }
        }

        // Sort by urgency (lowest attendance rate first)
        urgentCamps.sort((c1, c2) -> {
            double rate1 = (double) c1.get("attendanceRate");
            double rate2 = (double) c2.get("attendanceRate");
            return Double.compare(rate1, rate2);
        });

        return urgentCamps;
    }

    /**
     * Get camp statistics summary for dashboard
     */
    public Map<String, Object> getCampStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<BloodDonationCamp> allVerifiedCamps = getAllActiveVerifiedCamps();

        int totalCamps = allVerifiedCamps.size();
        int todaysCamps = (int) allVerifiedCamps.stream()
                .filter(camp -> camp.getCampDate().equals(LocalDate.now()))
                .count();
        int upcomingCamps = (int) allVerifiedCamps.stream()
                .filter(camp -> camp.getCampDate().isAfter(LocalDate.now()))
                .count();
        int totalCapacity = allVerifiedCamps.stream()
                .mapToInt(camp -> camp.getMaxDonors() != null ? camp.getMaxDonors() : 0)
                .sum();
        int totalRegistered = allVerifiedCamps.stream()
                .mapToInt(camp -> camp.getCurrentDonors() != null ? camp.getCurrentDonors() : 0)
                .sum();

        summary.put("totalCamps", totalCamps);
        summary.put("todaysCamps", todaysCamps);
        summary.put("upcomingCamps", upcomingCamps);
        summary.put("totalCapacity", totalCapacity);
        summary.put("totalRegistered", totalRegistered);
        summary.put("availableSlots", Math.max(0, totalCapacity - totalRegistered));
        summary.put("registrationRate", totalCapacity > 0 ? (totalRegistered * 100.0) / totalCapacity : 0);

        return summary;
    }

    // =============================================
    // ENHANCED METHODS FOR ATTENDANCE MANAGEMENT
    // =============================================

    /**
     * Get camps that need immediate attention (for dashboard alerts)
     */
    public List<Map<String, Object>> getCampsNeedingAttention() {
        List<BloodDonationCamp> todaysCamps = getTodaysVerifiedCamps();
        List<Map<String, Object>> attentionCamps = new ArrayList<>();

        for (BloodDonationCamp camp : todaysCamps) {
            Map<String, Object> campDetails = getCampDetailsForAttendance(camp.getId());
            double attendanceRate = (double) campDetails.get("attendanceRate");
            long registeredCount = (long) campDetails.get("registeredCount");

            // Camps need attention if they have low attendance or are starting soon
            if (registeredCount > 0 && attendanceRate < 40.0) {
                campDetails.put("attentionReason", "LOW_ATTENDANCE");
                campDetails.put("priority", "HIGH");
                attentionCamps.add(campDetails);
            } else if (camp.getStartTime() != null &&
                    LocalTime.now().isAfter(camp.getStartTime().minusHours(1)) &&
                    LocalTime.now().isBefore(camp.getStartTime())) {
                campDetails.put("attentionReason", "STARTING_SOON");
                campDetails.put("priority", "MEDIUM");
                attentionCamps.add(campDetails);
            }
        }

        return attentionCamps;
    }

    /**
     * Get volunteer's camps with detailed statistics
     */
    public List<Map<String, Object>> getVolunteerCampsWithStats(Long volunteerId) {
        List<BloodDonationCamp> volunteerCamps = getCampsByVolunteer(volunteerId);
        List<Map<String, Object>> campsWithStats = new ArrayList<>();

        for (BloodDonationCamp camp : volunteerCamps) {
            Map<String, Object> campStats = new HashMap<>();
            campStats.put("camp", camp);

            long registeredCount = campRegistrationRepository.countActiveRegistrationsByCampId(camp.getId());
            campStats.put("registeredCount", registeredCount);
            campStats.put("attendanceRate", registeredCount > 0 ? (camp.getCurrentDonors() * 100.0) / registeredCount : 0);
            campStats.put("availableSlots", Math.max(0, camp.getMaxDonors() - (int) registeredCount));

            campsWithStats.add(campStats);
        }

        return campsWithStats;
    }

    /**
     * Get camps by date range for reporting
     */
    public List<BloodDonationCamp> getCampsByDateRange(LocalDate startDate, LocalDate endDate) {
        return campRepository.findCampsByDateRange(startDate, endDate);
    }

    /**
     * Search camps with enhanced filters
     */
    public List<BloodDonationCamp> searchCampsWithFilters(String searchQuery, String status, String organizer) {
        List<BloodDonationCamp> allCamps = getAllActiveVerifiedCamps();

        return allCamps.stream()
                .filter(camp -> {
                    // Search filter
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        String searchTerm = searchQuery.toLowerCase().trim();
                        boolean matchesSearch = camp.getCampName().toLowerCase().contains(searchTerm) ||
                                camp.getLocation().toLowerCase().contains(searchTerm);
                        if (!matchesSearch) return false;
                    }

                    // Status filter
                    if (status != null && !status.isEmpty()) {
                        switch (status) {
                            case "today":
                                if (!camp.getCampDate().equals(LocalDate.now())) return false;
                                break;
                            case "upcoming":
                                if (!camp.getCampDate().isAfter(LocalDate.now())) return false;
                                break;
                            case "completed":
                                if (!camp.getCampDate().isBefore(LocalDate.now())) return false;
                                break;
                        }
                    }

                    // Organizer filter
                    if (organizer != null && !organizer.isEmpty()) {
                        String organizerName = camp.getOrganizedBy().getFirstName() + " " + camp.getOrganizedBy().getLastName();
                        if (!organizerName.toLowerCase().contains(organizer.toLowerCase())) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get camp performance metrics
     */
    public Map<String, Object> getCampPerformanceMetrics(Long campId) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null) return metrics;

            long registeredCount = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            double attendanceRate = registeredCount > 0 ? (camp.getCurrentDonors() * 100.0) / registeredCount : 0;
            int capacityUtilization = camp.getMaxDonors() > 0 ? (int) ((registeredCount * 100.0) / camp.getMaxDonors()) : 0;

            metrics.put("camp", camp);
            metrics.put("registeredCount", registeredCount);
            metrics.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            metrics.put("capacityUtilization", capacityUtilization);
            metrics.put("availableSlots", Math.max(0, camp.getMaxDonors() - (int) registeredCount));
            metrics.put("performanceScore", calculatePerformanceScore(attendanceRate, capacityUtilization));

        } catch (Exception e) {
            // Return empty metrics on error
        }
        return metrics;
    }

    private int calculatePerformanceScore(double attendanceRate, int capacityUtilization) {
        // Simple scoring algorithm based on attendance and capacity utilization
        double score = (attendanceRate * 0.6) + (capacityUtilization * 0.4);
        return (int) Math.min(100, Math.max(0, score));
    }

    // =============================================
    // REGISTRATION AND DONOR VERIFICATION METHODS
    // =============================================

    public boolean canDonorRegisterForCamp(Long campId, User donor) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null || !camp.getIsActive()) {
                return false;
            }

            if (!"DONOR".equals(donor.getUser_type()) || !donor.getIsVerified()) {
                return false;
            }

            if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
                return false;
            }

            if (camp.getCampDate().isBefore(LocalDate.now())) {
                return false;
            }

            long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            return currentRegistrations < camp.getMaxDonors();

        } catch (Exception e) {
            return false;
        }
    }

    public List<BloodDonationCamp> getAvailableCampsForDonor(User donor) {
        if (!"DONOR".equals(donor.getUser_type()) || !donor.getIsVerified()) {
            return new ArrayList<>();
        }

        List<BloodDonationCamp> allActiveCamps = getAllActiveCamps();
        return allActiveCamps.stream()
                .filter(camp ->
                        camp.getOrganizedBy().getIsVolunteerVerified() &&
                                (camp.getCampDate().isAfter(LocalDate.now()) ||
                                        camp.getCampDate().equals(LocalDate.now())) &&
                                camp.getIsActive()
                )
                .sorted((c1, c2) -> c1.getCampDate().compareTo(c2.getCampDate()))
                .collect(Collectors.toList());
    }

    public List<BloodDonationCamp> getCampsByVerifiedVolunteers() {
        List<BloodDonationCamp> allActiveCamps = getAllActiveCamps();
        return allActiveCamps.stream()
                .filter(camp ->
                        camp.getOrganizedBy().getIsVolunteerVerified() &&
                                camp.getIsActive()
                )
                .collect(Collectors.toList());
    }

    public boolean hasAvailableSlots(Long campId) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null) return false;

            long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            return currentRegistrations < camp.getMaxDonors();
        } catch (Exception e) {
            return false;
        }
    }

    public int getAvailableSlots(Long campId) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null) return 0;

            long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
            return Math.max(0, camp.getMaxDonors() - (int) currentRegistrations);
        } catch (Exception e) {
            return 0;
        }
    }

    public void validateCampRegistrationEligibility(Long campId, User donor) throws Exception {
        BloodDonationCamp camp = getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getIsActive()) {
            throw new Exception("This camp is no longer active");
        }

        if (!"DONOR".equals(donor.getUser_type())) {
            throw new Exception("Only donors can register for camps");
        }

        if (!donor.getIsVerified()) {
            throw new Exception("Only verified donors can register for camps. Please complete your verification first.");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("This camp is organized by an unverified volunteer");
        }

        if (camp.getCampDate().isBefore(LocalDate.now())) {
            throw new Exception("Cannot register for past camps");
        }

        long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
        if (currentRegistrations >= camp.getMaxDonors()) {
            throw new Exception("Camp is at full capacity");
        }

        if (campRegistrationRepository.existsByCampIdAndDonorIdAndStatus(campId, donor.getId(), "REGISTERED")) {
            throw new Exception("You are already registered for this camp");
        }
    }

    public void updateCampCapacityAfterRegistration(Long campId) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp != null) {
                long currentRegistrations = campRegistrationRepository.countActiveRegistrationsByCampId(campId);
                camp.setCurrentDonors((int) currentRegistrations);
                camp.setUpdatedAt(LocalDateTime.now());
                campRepository.save(camp);
            }
        } catch (Exception e) {
            System.err.println("Error updating camp capacity: " + e.getMessage());
        }
    }

    public boolean canViewCampDetails(Long campId, User user) {
        try {
            BloodDonationCamp camp = getCampById(campId);
            if (camp == null || !camp.getIsActive()) {
                return false;
            }

            if ("VOLUNTEER".equals(user.getUser_type())) {
                return true;
            }

            if ("DONOR".equals(user.getUser_type())) {
                return camp.getOrganizedBy().getIsVolunteerVerified();
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public List<BloodDonationCamp> getDonorRegisteredCamps(Long donorId) {
        List<CampRegistration> registrations = campRegistrationRepository.findByDonorIdAndStatus(donorId, "REGISTERED");
        return registrations.stream()
                .map(CampRegistration::getCamp)
                .filter(camp -> camp.getIsActive() &&
                        (camp.getCampDate().isAfter(LocalDate.now()) ||
                                camp.getCampDate().equals(LocalDate.now())))
                .collect(Collectors.toList());
    }

    /**
     * Get camp statistics for volunteer dashboard
     */
    public CampStatistics getCampStatistics(Long volunteerId) {
        List<BloodDonationCamp> volunteerCamps = getCampsByVolunteer(volunteerId);

        int totalCamps = volunteerCamps.size();
        int upcomingCamps = (int) volunteerCamps.stream()
                .filter(camp -> camp.getCampDate().isAfter(LocalDate.now()) ||
                        camp.getCampDate().equals(LocalDate.now()))
                .count();
        int totalCapacity = volunteerCamps.stream()
                .mapToInt(camp -> camp.getMaxDonors() != null ? camp.getMaxDonors() : 0)
                .sum();
        int totalRegistered = volunteerCamps.stream()
                .mapToInt(camp -> camp.getCurrentDonors() != null ? camp.getCurrentDonors() : 0)
                .sum();

        return new CampStatistics(totalCamps, upcomingCamps, totalCapacity, totalRegistered);
    }

    /**
     * Get global camp statistics for all verified camps
     */
    public GlobalCampStatistics getGlobalCampStatistics() {
        List<BloodDonationCamp> allVerifiedCamps = getAllActiveVerifiedCamps();

        int totalCamps = allVerifiedCamps.size();
        int todaysCamps = (int) allVerifiedCamps.stream()
                .filter(camp -> camp.getCampDate().equals(LocalDate.now()))
                .count();
        int upcomingCamps = (int) allVerifiedCamps.stream()
                .filter(camp -> camp.getCampDate().isAfter(LocalDate.now()))
                .count();
        int totalCapacity = allVerifiedCamps.stream()
                .mapToInt(camp -> camp.getMaxDonors() != null ? camp.getMaxDonors() : 0)
                .sum();
        int totalRegistered = allVerifiedCamps.stream()
                .mapToInt(camp -> camp.getCurrentDonors() != null ? camp.getCurrentDonors() : 0)
                .sum();

        return new GlobalCampStatistics(totalCamps, todaysCamps, upcomingCamps, totalCapacity, totalRegistered);
    }

    /**
     * Validate camp for attendance recording
     */
    public void validateCampForAttendance(Long campId, User volunteer) throws Exception {
        BloodDonationCamp camp = getCampById(campId);
        if (camp == null) {
            throw new Exception("Camp not found");
        }

        if (!camp.getIsActive()) {
            throw new Exception("Camp is not active");
        }

        if (!camp.getOrganizedBy().getIsVolunteerVerified()) {
            throw new Exception("Camp organizer is not verified");
        }

        if (!volunteer.getIsVolunteerVerified()) {
            throw new Exception("Only verified volunteers can record attendance");
        }

        if (camp.getCampDate().isBefore(LocalDate.now())) {
            throw new Exception("Cannot record attendance for past camps");
        }
    }

    // =============================================
    // INNER CLASSES
    // =============================================

    public static class CampStatistics {
        private final int totalCamps;
        private final int upcomingCamps;
        private final int totalCapacity;
        private final int totalRegistered;

        public CampStatistics(int totalCamps, int upcomingCamps, int totalCapacity, int totalRegistered) {
            this.totalCamps = totalCamps;
            this.upcomingCamps = upcomingCamps;
            this.totalCapacity = totalCapacity;
            this.totalRegistered = totalRegistered;
        }

        public int getTotalCamps() { return totalCamps; }
        public int getUpcomingCamps() { return upcomingCamps; }
        public int getTotalCapacity() { return totalCapacity; }
        public int getTotalRegistered() { return totalRegistered; }
    }

    public static class GlobalCampStatistics {
        private final int totalCamps;
        private final int todaysCamps;
        private final int upcomingCamps;
        private final int totalCapacity;
        private final int totalRegistered;

        public GlobalCampStatistics(int totalCamps, int todaysCamps, int upcomingCamps, int totalCapacity, int totalRegistered) {
            this.totalCamps = totalCamps;
            this.todaysCamps = todaysCamps;
            this.upcomingCamps = upcomingCamps;
            this.totalCapacity = totalCapacity;
            this.totalRegistered = totalRegistered;
        }

        public int getTotalCamps() { return totalCamps; }
        public int getTodaysCamps() { return todaysCamps; }
        public int getUpcomingCamps() { return upcomingCamps; }
        public int getTotalCapacity() { return totalCapacity; }
        public int getTotalRegistered() { return totalRegistered; }
    }
}