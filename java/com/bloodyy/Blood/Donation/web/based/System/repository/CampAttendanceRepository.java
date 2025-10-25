// File: CampAttendanceRepository.java
package com.bloodyy.Blood.Donation.web.based.System.repository;

import com.bloodyy.Blood.Donation.web.based.System.entity.CampAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CampAttendanceRepository extends JpaRepository<CampAttendance, Long> {

    List<CampAttendance> findByCampIdOrderByAttendedAtDesc(Long campId);

    Optional<CampAttendance> findByCampIdAndDonorId(Long campId, Long donorId);

    @Query("SELECT ca FROM CampAttendance ca WHERE ca.camp.id = :campId AND ca.bloodDonated = true")
    List<CampAttendance> findDonorsWhoDonatedBlood(@Param("campId") Long campId);

    @Query("SELECT COUNT(ca) FROM CampAttendance ca WHERE ca.camp.id = :campId AND ca.bloodDonated = true")
    long countBloodDonationsByCamp(@Param("campId") Long campId);

    @Query("SELECT ca FROM CampAttendance ca WHERE ca.camp.id = :campId AND " +
            "(LOWER(ca.donor.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ca.donor.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(ca.donor.firstName, ' ', ca.donor.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<CampAttendance> searchAttendeesByName(@Param("campId") Long campId, @Param("search") String search);

    boolean existsByCampIdAndDonorId(Long campId, Long donorId);

    // ADD THIS METHOD - Fix for missing implementation
    @Query("SELECT COUNT(ca) FROM CampAttendance ca WHERE ca.camp.id = :campId")
    long countByCampId(@Param("campId") Long campId);
}