package com.bloodyy.Blood.Donation.web.based.System.repository;

import com.bloodyy.Blood.Donation.web.based.System.entity.BloodDonationCamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BloodDonationCampRepository extends JpaRepository<BloodDonationCamp, Long> {

    List<BloodDonationCamp> findByOrganizedByIdAndIsActiveOrderByCampDateDesc(Long volunteerId, Boolean isActive);

    List<BloodDonationCamp> findByIsActiveOrderByCampDateDesc(Boolean isActive);

    List<BloodDonationCamp> findByCampDateAfterAndIsActiveOrderByCampDate(LocalDate date, Boolean isActive);

    // ADD THIS METHOD: Find today's camps
    @Query("SELECT c FROM BloodDonationCamp c WHERE c.campDate = :date AND c.isActive = true")
    List<BloodDonationCamp> findByCampDateAndIsActive(@Param("date") LocalDate date);

    @Query("SELECT c FROM BloodDonationCamp c WHERE c.organizedBy.id = :volunteerId AND c.isActive = true ORDER BY c.campDate DESC")
    List<BloodDonationCamp> findActiveCampsByVolunteer(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COUNT(c) FROM BloodDonationCamp c WHERE c.organizedBy.id = :volunteerId AND c.isActive = true")
    long countActiveCampsByVolunteer(@Param("volunteerId") Long volunteerId);

    // ADD THIS METHOD: Find camps by date range
    @Query("SELECT c FROM BloodDonationCamp c WHERE c.campDate BETWEEN :startDate AND :endDate AND c.isActive = true ORDER BY c.campDate, c.startTime")
    List<BloodDonationCamp> findCampsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ADD THIS METHOD: Search camps by name or location
    @Query("SELECT c FROM BloodDonationCamp c WHERE (LOWER(c.campName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.location) LIKE LOWER(CONCAT('%', :search, '%'))) AND c.isActive = true")
    List<BloodDonationCamp> searchCamps(@Param("search") String search);
}