package com.bloodyy.Blood.Donation.web.based.System.repository;

import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.user_type = :userType")
    List<User> findByUser_type(@Param("userType") String userType);

    @Query("SELECT u FROM User u WHERE u.user_type IN :userTypes ORDER BY u.firstName, u.lastName")
    List<User> findByUserTypes(@Param("userTypes") List<String> userTypes);

    // Add this method for counting by user type
    @Query("SELECT COUNT(u) FROM User u WHERE u.user_type = :userType")
    long countByUser_type(@Param("userType") String userType);

    // Search donors by first name, last name, or full name
    @Query("SELECT u FROM User u WHERE u.user_type = 'DONOR' AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findDonorsByName(@Param("search") String search);


    // Search volunteers by first name, last name, or full name
    @Query("SELECT u FROM User u WHERE u.user_type = 'VOLUNTEER' AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findVolunteersByName(@Param("search") String search);


    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);
    // Check if phone exists excluding a specific user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone = :phone AND u.id != :excludeUserId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeUserId") Long excludeUserId);

}
