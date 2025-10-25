// File: CampRegistrationRepository.java
package com.bloodyy.Blood.Donation.web.based.System.repository;

import com.bloodyy.Blood.Donation.web.based.System.entity.CampRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CampRegistrationRepository extends JpaRepository<CampRegistration, Long> {

    boolean existsByCampIdAndDonorIdAndStatus(Long campId, Long donorId, String status);

    List<CampRegistration> findByCampIdAndStatus(Long campId, String status);

    List<CampRegistration> findByDonorIdAndStatus(Long donorId, String status);

    Optional<CampRegistration> findByCampIdAndDonorId(Long campId, Long donorId);

    @Query("SELECT COUNT(cr) FROM CampRegistration cr WHERE cr.camp.id = :campId AND cr.status = 'REGISTERED'")
    long countActiveRegistrationsByCampId(@Param("campId") Long campId);

    @Query("SELECT cr FROM CampRegistration cr WHERE cr.donor.id = :donorId AND cr.camp.isActive = true")
    List<CampRegistration> findActiveRegistrationsByDonor(@Param("donorId") Long donorId);
}