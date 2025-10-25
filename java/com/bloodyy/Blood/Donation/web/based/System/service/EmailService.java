package com.bloodyy.Blood.Donation.web.based.System.service;

/**
 * EmailService interface for Blood Donation Management System
 * Provides comprehensive email notification functionality including FIFO operations
 */
public interface EmailService {

    // ===== BASIC USER MANAGEMENT EMAILS =====

    /**
     * Send temporary password email to new users
     * @param toEmail recipient email address
     * @param firstName user's first name
     * @param lastName user's last name
     * @param userType user role (MEDICAL_STAFF, HOSPITAL_STAFF, etc.)
     * @param temporaryPassword generated temporary password
     */
    void sendTemporaryPasswordEmail(String toEmail, String firstName, String lastName,
                                    String userType, String temporaryPassword);

    /**
     * Send welcome email to new users (alias for temporary password email)
     * @param toEmail recipient email address
     * @param firstName user's first name
     * @param lastName user's last name
     * @param userType user role
     * @param temporaryPassword generated temporary password
     */
    void sendWelcomeEmail(String toEmail, String firstName, String lastName,
                          String userType, String temporaryPassword);

    // ===== BLOOD REQUEST WORKFLOW EMAILS =====

    /**
     * Notify medical staff of new blood request
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param requestId blood request ID
     * @param bloodType requested blood type
     * @param quantity requested quantity
     * @param urgency request urgency level
     * @param hospitalName requesting hospital
     */
    void sendBloodRequestNotification(String toEmail, String firstName, Long requestId,
                                      String bloodType, Integer quantity, String urgency,
                                      String hospitalName);

    /**
     * Notify hospital staff that request was approved
     * @param toEmail hospital staff email
     * @param firstName staff first name
     * @param requestId blood request ID
     * @param bloodType approved blood type
     * @param quantity approved quantity
     * @param approvedBy medical staff who approved
     */
    void sendRequestApprovalNotification(String toEmail, String firstName, Long requestId,
                                         String bloodType, Integer quantity, String approvedBy);

    /**
     * Notify hospital staff that request was rejected
     * @param toEmail hospital staff email
     * @param firstName staff first name
     * @param requestId blood request ID
     * @param bloodType rejected blood type
     * @param quantity rejected quantity
     * @param rejectionReason reason for rejection
     */
    void sendRequestRejectionNotification(String toEmail, String firstName, Long requestId,
                                          String bloodType, Integer quantity, String rejectionReason);

    // ===== FIFO-SPECIFIC WORKFLOW EMAILS =====

    /**
     * Notify hospital staff that request was fulfilled using FIFO system
     * @param toEmail hospital staff email
     * @param firstName staff first name
     * @param requestId fulfilled request ID
     * @param bloodType fulfilled blood type
     * @param quantity fulfilled quantity
     * @param fulfilledBy medical staff who fulfilled request
     */
    void sendRequestFulfillmentNotification(String toEmail, String firstName, Long requestId,
                                            String bloodType, Integer quantity, String fulfilledBy);

    /**
     * Notify medical staff of blood usage through FIFO system
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType blood type used
     * @param quantity quantity used
     * @param reason reason for usage
     * @param usedBy staff member who processed usage
     */
    void sendBloodUsageNotification(String toEmail, String firstName, String bloodType,
                                    Integer quantity, String reason, String usedBy);

    /**
     * Send detailed FIFO processing notification
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType processed blood type
     * @param totalUsed total units processed
     * @param oldestBatchUsed batch number of oldest blood used
     * @param reason reason for processing
     */
    void sendFIFOProcessingNotification(String toEmail, String firstName, String bloodType,
                                        Integer totalUsed, String oldestBatchUsed, String reason);

    // ===== INVENTORY MANAGEMENT EMAILS =====

    /**
     * Alert medical staff of low inventory levels
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType blood type with low inventory
     * @param currentStock current stock level
     * @param threshold minimum threshold level
     */
    void sendLowInventoryAlert(String toEmail, String firstName, String bloodType,
                               Integer currentStock, Integer threshold);

    /**
     * Alert medical staff of expired blood requiring removal
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType expired blood type
     * @param batchNumber expired batch number
     * @param expirationDate expiration date
     */
    void sendExpiredBloodAlert(String toEmail, String firstName, String bloodType,
                               String batchNumber, String expirationDate);

    /**
     * Notify medical staff of inventory replenishment
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType replenished blood type
     * @param addedQuantity quantity added
     * @param batchNumber new batch number
     */
    void sendInventoryReplenishmentNotification(String toEmail, String firstName, String bloodType,
                                                Integer addedQuantity, String batchNumber);

    // ===== ADVANCED INVENTORY ALERTS =====

    /**
     * Send critical shortage alert for multiple blood types
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param criticalBloodTypes map of blood types and their current levels
     * @param totalAffectedRequests number of pending requests affected
     */
    void sendCriticalShortageAlert(String toEmail, String firstName,
                                   java.util.Map<String, Integer> criticalBloodTypes,
                                   Integer totalAffectedRequests);

    /**
     * Send batch depletion notification when FIFO processing empties a batch
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param bloodType depleted blood type
     * @param depletedBatchNumber batch that was completely used
     * @param remainingBatches number of remaining batches for this blood type
     * @param usageReason reason for depletion
     */
    void sendBatchDepletionNotification(String toEmail, String firstName, String bloodType,
                                        String depletedBatchNumber, Integer remainingBatches,
                                        String usageReason);

    /**
     * Send weekly inventory summary with FIFO statistics
     * @param toEmail medical staff email
     * @param firstName staff first name
     * @param weeklyStats map containing weekly statistics
     * @param upcomingExpirations list of batches expiring soon
     */
    void sendWeeklyInventorySummary(String toEmail, String firstName,
                                    java.util.Map<String, Object> weeklyStats,
                                    java.util.List<String> upcomingExpirations);

    // ===== DONOR COMMUNICATION EMAILS =====

    /**
     * Send donation request to donors when their blood type is needed
     * @param toEmail donor email
     * @param firstName donor first name
     * @param bloodType needed blood type
     * @param urgencyLevel urgency of need
     * @param estimatedShortage estimated shortage amount
     * @param donationCenter nearest donation center
     */
    void sendDonationRequestToDonor(String toEmail, String firstName, String bloodType,
                                    String urgencyLevel, Integer estimatedShortage,
                                    String donationCenter);

    /**
     * Send thank you email to donor after donation
     * @param toEmail donor email
     * @param firstName donor first name
     * @param bloodType donated blood type
     * @param donationDate date of donation
     * @param impactMessage personalized impact message
     */
    void sendDonationThankYou(String toEmail, String firstName, String bloodType,
                              String donationDate, String impactMessage);

    // ===== SYSTEM NOTIFICATION EMAILS =====

    /**
     * Send system maintenance notification
     * @param toEmail user email
     * @param firstName user first name
     * @param maintenanceStartTime scheduled start time
     * @param estimatedDuration estimated duration
     * @param affectedFeatures list of affected features
     */
    void sendSystemMaintenanceNotification(String toEmail, String firstName,
                                           String maintenanceStartTime, String estimatedDuration,
                                           java.util.List<String> affectedFeatures);

    /**
     * Send emergency blood shortage alert to all relevant staff
     * @param toEmail staff email
     * @param firstName staff first name
     * @param criticalBloodTypes list of critically short blood types
     * @param emergencyContactInfo emergency contact information
     * @param immediateActions list of immediate actions required
     */
    void sendEmergencyShortageAlert(String toEmail, String firstName,
                                    java.util.List<String> criticalBloodTypes,
                                    String emergencyContactInfo,
                                    java.util.List<String> immediateActions);

    // ===== REPORTING AND ANALYTICS EMAILS =====

    /**
     * Send monthly performance report
     * @param toEmail recipient email
     * @param firstName recipient first name
     * @param reportPeriod reporting period
     * @param performanceMetrics map of performance metrics
     * @param fifoEfficiencyStats FIFO system efficiency statistics
     */
    void sendMonthlyPerformanceReport(String toEmail, String firstName, String reportPeriod,
                                      java.util.Map<String, Object> performanceMetrics,
                                      java.util.Map<String, Double> fifoEfficiencyStats);

    /**
     * Send quality assurance alert
     * @param toEmail quality assurance staff email
     * @param firstName staff first name
     * @param issueType type of quality issue
     * @param affectedBatches list of affected batch numbers
     * @param immediateActions required immediate actions
     * @param investigationRequired whether investigation is needed
     */
    void sendQualityAssuranceAlert(String toEmail, String firstName, String issueType,
                                   java.util.List<String> affectedBatches,
                                   java.util.List<String> immediateActions,
                                   Boolean investigationRequired);

    // ===== COMPLIANCE AND AUDIT EMAILS =====

    /**
     * Send audit trail notification
     * @param toEmail compliance officer email
     * @param firstName officer first name
     * @param auditEventType type of audit event
     * @param eventDetails details of the event
     * @param affectedRecords number of affected records
     * @param complianceStatus current compliance status
     */
    void sendAuditTrailNotification(String toEmail, String firstName, String auditEventType,
                                    String eventDetails, Integer affectedRecords,
                                    String complianceStatus);

    /**
     * Send temperature excursion alert for blood storage
     * @param toEmail facility manager email
     * @param firstName manager first name
     * @param storageLocation affected storage location
     * @param temperatureRange recorded temperature range
     * @param duration duration of excursion
     * @param affectedInventory list of affected inventory
     */
    void sendTemperatureExcursionAlert(String toEmail, String firstName, String storageLocation,
                                       String temperatureRange, String duration,
                                       java.util.List<String> affectedInventory);


    // Add to EmailService interface
    void sendEmailChangeConfirmation(String toEmail, String firstName, String lastName,
                                     String userType, String temporaryPassword);


    // Add this method to EmailService interface
    void sendEmailChangeCancellationNotification(String toEmail, String firstName, String lastName,
                                                 Long requestId, String cancellationReason);


    // Add to EmailService.java
    /**
     * Send password reset instructions with reset link
     */
    void sendPasswordResetEmail(String toEmail, String firstName, String resetLink);

    /**
     * Send new temporary password after reset
     */
    void sendNewPasswordEmail(String toEmail, String firstName, String temporaryPassword);
}