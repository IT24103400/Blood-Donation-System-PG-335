package com.bloodyy.Blood.Donation.web.based.System.service;

import com.bloodyy.Blood.Donation.web.based.System.entity.EmailChangeRequest;
import com.bloodyy.Blood.Donation.web.based.System.entity.User;
import com.bloodyy.Blood.Donation.web.based.System.repository.EmailChangeRequestRepository;
import com.bloodyy.Blood.Donation.web.based.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailChangeService {

    @Autowired
    private EmailChangeRequestRepository emailChangeRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    public EmailChangeRequest submitEmailChangeRequest(String currentEmail, String newEmail,
                                                       String fullName, String reason) {

        // Validate required fields
        if (currentEmail == null || currentEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Current email cannot be empty");
        }

        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("New email cannot be empty");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        // Check if emails are the same
        if (currentEmail.equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email must be different from current email");
        }

        // Check if new email already exists in users
        if (userService.emailExists(newEmail)) {
            throw new IllegalArgumentException("New email is already registered in the system");
        }

        // Check if user exists with current email
        User existingUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (existingUser == null) {
            throw new IllegalArgumentException("No user found with the current email address");
        }

        // Check for duplicate pending requests
        List<EmailChangeRequest> existingRequests = emailChangeRequestRepository
                .findByCurrentEmailAndIsProcessedFalse(currentEmail);
        if (!existingRequests.isEmpty()) {
            throw new IllegalArgumentException("You already have a pending email change request. Please wait for it to be processed.");
        }

        // Create and save the request
        EmailChangeRequest request = new EmailChangeRequest();
        request.setCurrentEmail(currentEmail.trim().toLowerCase());
        request.setNewEmail(newEmail.trim().toLowerCase());
        request.setFullName(fullName.trim());
        request.setReason(reason != null ? reason.trim() : "No reason provided");
        request.setRequestedAt(LocalDateTime.now());
        request.setIsProcessed(false);

        EmailChangeRequest savedRequest = emailChangeRequestRepository.save(request);

        // Log the request for debugging
        System.out.println("Email change request saved: " + savedRequest.getId() +
                " for user: " + fullName + " from " + currentEmail + " to " + newEmail);

        return savedRequest;
    }

    public List<EmailChangeRequest> getPendingRequests() {
        return emailChangeRequestRepository.findByIsProcessedFalseOrderByRequestedAtDesc();
    }

    public List<EmailChangeRequest> searchPendingRequests(String searchQuery) {
        return emailChangeRequestRepository.searchPendingRequests(searchQuery.toLowerCase());
    }

    public boolean processEmailChange(Long requestId, Long adminId) {
        EmailChangeRequest request = emailChangeRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getIsProcessed()) {
            System.out.println("Request not found or already processed: " + requestId);
            return false;
        }

        // Find user by current email
        User user = userRepository.findByEmail(request.getCurrentEmail()).orElse(null);
        if (user == null) {
            System.out.println("User not found with email: " + request.getCurrentEmail());
            return false;
        }

        // Check if new email is already taken by another user
        if (userService.emailExists(request.getNewEmail())) {
            System.out.println("New email already exists: " + request.getNewEmail());
            return false;
        }

        try {
            // Generate temporary password
            String tempPassword = userService.generateTemporaryPassword();

            // Update user email and set temporary password
            String oldEmail = user.getEmail();
            user.setEmail(request.getNewEmail());
            user.setPassword(tempPassword);
            user.setIsTempPassword(true);
            userRepository.save(user);

            System.out.println("User email updated from " + oldEmail + " to " + request.getNewEmail());

            // Send email with new credentials
            emailService.sendEmailChangeConfirmation(
                    request.getNewEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUser_type(),
                    tempPassword
            );

            // Mark request as processed
            request.setIsProcessed(true);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminId);
            emailChangeRequestRepository.save(request);

            System.out.println("Email change request processed successfully: " + requestId);
            return true;

        } catch (Exception e) {
            System.err.println("Error processing email change request " + requestId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancel/Decline an email change request
     */
    public boolean cancelEmailChangeRequest(Long requestId, Long adminId, String cancellationReason) {
        EmailChangeRequest request = emailChangeRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getIsProcessed()) {
            System.out.println("Request not found or already processed: " + requestId);
            return false;
        }

        try {
            // Find user by current email to send notification
            User user = userRepository.findByEmail(request.getCurrentEmail()).orElse(null);

            // Mark request as processed (cancelled)
            request.setIsProcessed(true);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminId);

            // Store cancellation reason along with original reason
            String originalReason = request.getReason();
            if (cancellationReason != null && !cancellationReason.trim().isEmpty()) {
                request.setReason("[CANCELLED] Reason: " + cancellationReason + " | Original: " + originalReason);
            } else {
                request.setReason("[CANCELLED] No reason provided | Original: " + originalReason);
            }

            emailChangeRequestRepository.save(request);

            // Send cancellation notification email to user
            if (user != null) {
                emailService.sendEmailChangeCancellationNotification(
                        request.getCurrentEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        requestId,
                        cancellationReason
                );
            }

            System.out.println("Email change request cancelled successfully: " + requestId);
            return true;

        } catch (Exception e) {
            System.err.println("Error cancelling email change request " + requestId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get email change request by ID
     */
    public EmailChangeRequest getRequestById(Long requestId) {
        return emailChangeRequestRepository.findById(requestId).orElse(null);
    }

    /**
     * Additional helper method to check for existing pending requests
     */
    public List<EmailChangeRequest> findByCurrentEmailAndIsProcessedFalse(String email) {
        return emailChangeRequestRepository.findByCurrentEmailAndIsProcessedFalse(email);
    }
}