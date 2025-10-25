package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.dto.PasswordChangeDTO;
import com.bloodyy.Blood.Donation.web.based.System.dto.UserCreationDTO;
import com.bloodyy.Blood.Donation.web.based.System.dto.UserRegistrationDTO;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @PostConstruct
    public void initAdmin() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setFirstName(adminName.split(" ")[0]);
            admin.setLastName(adminName.split(" ")[1]);
            admin.setEmail(adminEmail);
            admin.setPassword(adminPassword);
            admin.setUser_type("SYSTEM_ADMIN");
            admin.setIsTempPassword(false);
            userRepository.save(admin);
        }
    }

    public void registerUser(UserRegistrationDTO registrationDTO) throws Exception {
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new Exception("Passwords do not match");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new Exception("Email already exists");
        }

        if (registrationDTO.getPhone() != null &&
                !registrationDTO.getPhone().trim().isEmpty() &&
                userRepository.existsByPhone(registrationDTO.getPhone())) {
            throw new Exception("Phone number already exists");
        }

        if ("DONOR".equals(registrationDTO.getUser_type()) &&
                (registrationDTO.getBloodType() == null || registrationDTO.getBloodType().trim().isEmpty())) {
            throw new Exception("Blood type is required for donors");
        }

        User user = new User();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(registrationDTO.getPassword());
        user.setUser_type(registrationDTO.getUser_type());
        user.setBloodType("DONOR".equals(registrationDTO.getUser_type()) ? registrationDTO.getBloodType() : null);
        user.setDateOfBirth(registrationDTO.getDateOfBirth());
        user.setPhone(registrationDTO.getPhone());
        user.setAddress(registrationDTO.getAddress());
        user.setIsTempPassword(false);

        userRepository.save(user);
    }

    public User createUser(UserCreationDTO userCreationDTO) throws Exception {
        if (userRepository.existsByEmail(userCreationDTO.getEmail())) {
            throw new Exception("Email already exists");
        }

        // Check unique phone number
        if (userCreationDTO.getPhone() != null &&
                !userCreationDTO.getPhone().trim().isEmpty() &&
                userRepository.existsByPhone(userCreationDTO.getPhone())) {
            throw new Exception("Phone number already exists");
        }

        String password = userCreationDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = generateTemporaryPassword();
        }

        User user = new User();
        user.setFirstName(userCreationDTO.getFirstName());
        user.setLastName(userCreationDTO.getLastName());
        user.setEmail(userCreationDTO.getEmail());
        user.setPassword(password);
        user.setUser_type(userCreationDTO.getUserType());
        user.setBloodType(userCreationDTO.getBloodType());
        user.setDateOfBirth(userCreationDTO.getDateOfBirth());
        user.setPhone(userCreationDTO.getPhone());
        user.setAddress(userCreationDTO.getAddress());
        user.setIsTempPassword(true);

        User savedUser = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getUser_type(),
                    password
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            throw new Exception("User created but failed to send email: " + e.getMessage());
        }

        return savedUser;
    }

    public String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public User loginUser(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public List<User> getUsersByType(String userType) {
        return userRepository.findByUser_type(userType);
    }

    public List<User> getStaffUsers() {
        List<String> staffTypes = List.of("MEDICAL_STAFF", "HOSPITAL_STAFF", "REGIONAL_LIAISON");
        return userRepository.findByUserTypes(staffTypes);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public boolean changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        if (!user.getPassword().equals(passwordChangeDTO.getCurrentPassword())) {
            return false;
        }

        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            return false;
        }

        user.setPassword(passwordChangeDTO.getNewPassword());
        user.setIsTempPassword(false);
        userRepository.save(user);

        return true;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public long countUsersByType(String userType) {
        return userRepository.countByUser_type(userType);
    }

    public List<User> getAllRegionalLiaisons() {
        return userRepository.findByUser_type("REGIONAL_LIAISON");
    }

    public User updateUserProfile(Long userId, String firstName, String lastName,
                                  String phone, String address) throws Exception {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Check if new phone number is unique (excluding current user)
            if (phone != null && !phone.trim().isEmpty() &&
                    !phone.equals(user.getPhone()) &&
                    userRepository.existsByPhoneAndIdNot(phone, userId)) {
                throw new Exception("Phone number already exists");
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setAddress(address);
            return userRepository.save(user);
        }
        return null;
    }

    // Add phone validation for donor updates
    public User updateUserProfileWithBloodType(Long userId, String firstName, String lastName,
                                               String phone, String address, String bloodType) throws Exception {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Check phone uniqueness
            if (phone != null && !phone.trim().isEmpty() &&
                    !phone.equals(user.getPhone()) &&
                    userRepository.existsByPhoneAndIdNot(phone, userId)) {
                throw new Exception("Phone number already exists");
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setAddress(address);

            if (user.getBloodType() == null && "DONOR".equals(user.getUser_type()) &&
                    bloodType != null && !bloodType.trim().isEmpty()) {
                user.setBloodType(bloodType);
            }

            return userRepository.save(user);
        }
        return null;
    }

    public User updateUserBloodType(Long userId, String newBloodType, User adminUser) throws Exception {
        if (!"SYSTEM_ADMIN".equals(adminUser.getUser_type())) {
            throw new Exception("Only system administrators can modify blood type");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && "DONOR".equals(user.getUser_type())) {
            user.setBloodType(newBloodType);
            return userRepository.save(user);
        }
        throw new Exception("User not found or not a donor");
    }

    public boolean deleteDonorAccount(Long userId, User requestingUser) throws Exception {
        if (!requestingUser.getId().equals(userId)) {
            throw new Exception("You can only delete your own account");
        }

        if (!"DONOR".equals(requestingUser.getUser_type())) {
            throw new Exception("Only donors can delete their own accounts");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new Exception("User not found");
        }

        try {
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            throw new Exception("Failed to delete account: " + e.getMessage());
        }
    }

    // ✅ Donor Management Methods

    public List<User> searchDonorsByName(String searchQuery) {
        return userRepository.findDonorsByName(searchQuery.toLowerCase());
    }

    public User verifyDonor(Long donorId, Long adminId) throws Exception {
        User donor = userRepository.findById(donorId).orElse(null);
        if (donor == null) {
            throw new Exception("Donor not found");
        }

        if (!"DONOR".equals(donor.getUser_type())) {
            throw new Exception("User is not a donor");
        }

        donor.setIsVerified(true);
        donor.setVerifiedBy(adminId);
        donor.setVerifiedAt(LocalDateTime.now());

        return userRepository.save(donor);
    }

    public User unverifyDonor(Long donorId) throws Exception {
        User donor = userRepository.findById(donorId).orElse(null);
        if (donor == null) {
            throw new Exception("Donor not found");
        }

        donor.setIsVerified(false);
        donor.setVerifiedBy(null);
        donor.setVerifiedAt(null);

        return userRepository.save(donor);
    }



    // ✅ Volunteer Management Methods

    public List<User> searchVolunteersByName(String searchQuery) {
        return userRepository.findVolunteersByName(searchQuery.toLowerCase());
    }

    public User verifyVolunteer(Long volunteerId, Long adminId) throws Exception {
        User volunteer = userRepository.findById(volunteerId).orElse(null);
        if (volunteer == null) {
            throw new Exception("Volunteer not found");
        }

        if (!"VOLUNTEER".equals(volunteer.getUser_type())) {
            throw new Exception("User is not a volunteer");
        }

        volunteer.setIsVolunteerVerified(true);
        volunteer.setVolunteerVerifiedBy(adminId);
        volunteer.setVolunteerVerifiedAt(LocalDateTime.now());

        return userRepository.save(volunteer);
    }

    public User unverifyVolunteer(Long volunteerId) throws Exception {
        User volunteer = userRepository.findById(volunteerId).orElse(null);
        if (volunteer == null) {
            throw new Exception("Volunteer not found");
        }

        volunteer.setIsVolunteerVerified(false);
        volunteer.setVolunteerVerifiedBy(null);
        volunteer.setVolunteerVerifiedAt(null);

        return userRepository.save(volunteer);
    }


}
