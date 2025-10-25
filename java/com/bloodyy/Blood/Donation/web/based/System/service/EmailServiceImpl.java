package com.bloodyy.Blood.Donation.web.based.System.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Override
    public void sendTemporaryPasswordEmail(String toEmail, String firstName, String lastName,
                                           String userType, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Give Blood, Give Life Account Credentials");

            String htmlContent = buildEmailContent(firstName, lastName, userType, temporaryPassword, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email to: " + toEmail, e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Unexpected error sending email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String firstName, String lastName,
                                 String userType, String temporaryPassword) {
        sendTemporaryPasswordEmail(toEmail, firstName, lastName, userType, temporaryPassword);
    }

    @Override
    public void sendBloodRequestNotification(String toEmail, String firstName, Long requestId,
                                             String bloodType, Integer quantity, String urgency, String hospitalName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Blood Request - Urgent Action Required");

            String htmlContent = buildBloodRequestNotificationContent(firstName, requestId, bloodType, quantity, urgency, hospitalName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Blood request notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send blood request notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendRequestApprovalNotification(String toEmail, String firstName, Long requestId,
                                                String bloodType, Integer quantity, String approvedBy) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Blood Request Approved - Request #" + requestId);

            String htmlContent = buildApprovalNotificationContent(firstName, requestId, bloodType, quantity, approvedBy);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Approval notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send approval notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendRequestRejectionNotification(String toEmail, String firstName, Long requestId,
                                                 String bloodType, Integer quantity, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Blood Request Rejected - Request #" + requestId);

            String htmlContent = buildRejectionNotificationContent(firstName, requestId, bloodType, quantity, rejectionReason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Rejection notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send rejection notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ===== FIFO-RELATED METHODS =====

    @Override
    public void sendRequestFulfillmentNotification(String toEmail, String firstName, Long requestId,
                                                   String bloodType, Integer quantity, String fulfilledBy) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Blood Request Fulfilled - Request #" + requestId);

            String htmlContent = buildFulfillmentNotificationContent(firstName, requestId, bloodType, quantity, fulfilledBy);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Fulfillment notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send fulfillment notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendBloodUsageNotification(String toEmail, String firstName, String bloodType,
                                           Integer quantity, String reason, String usedBy) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Blood Inventory Usage Alert - " + bloodType);

            String htmlContent = buildBloodUsageNotificationContent(firstName, bloodType, quantity, reason, usedBy);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Blood usage notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send blood usage notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendLowInventoryAlert(String toEmail, String firstName, String bloodType,
                                      Integer currentStock, Integer threshold) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LOW INVENTORY ALERT - " + bloodType + " Blood");

            String htmlContent = buildLowInventoryAlertContent(firstName, bloodType, currentStock, threshold);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Low inventory alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send low inventory alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendExpiredBloodAlert(String toEmail, String firstName, String bloodType,
                                      String batchNumber, String expirationDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("EXPIRED BLOOD ALERT - " + bloodType + " Batch " + batchNumber);

            String htmlContent = buildExpiredBloodAlertContent(firstName, bloodType, batchNumber, expirationDate);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Expired blood alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send expired blood alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendFIFOProcessingNotification(String toEmail, String firstName, String bloodType,
                                               Integer totalUsed, String oldestBatchUsed, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("FIFO Processing Complete - " + bloodType);

            String htmlContent = buildFIFOProcessingNotificationContent(firstName, bloodType, totalUsed, oldestBatchUsed, reason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("FIFO processing notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send FIFO processing notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendInventoryReplenishmentNotification(String toEmail, String firstName, String bloodType,
                                                       Integer addedQuantity, String batchNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Inventory Replenished - " + bloodType + " Blood Added");

            String htmlContent = buildInventoryReplenishmentContent(firstName, bloodType, addedQuantity, batchNumber);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Inventory replenishment notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send inventory replenishment notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ===== NEWLY ADDED MISSING METHODS =====

    @Override
    public void sendCriticalShortageAlert(String toEmail, String firstName,
                                          Map<String, Integer> criticalBloodTypes, Integer totalAffectedRequests) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("CRITICAL SHORTAGE ALERT - Multiple Blood Types");

            String htmlContent = buildCriticalShortageAlertContent(firstName, criticalBloodTypes, totalAffectedRequests);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Critical shortage alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send critical shortage alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendBatchDepletionNotification(String toEmail, String firstName, String bloodType,
                                               String depletedBatchNumber, Integer remainingBatches, String usageReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Batch Depleted - " + bloodType + " Batch " + depletedBatchNumber);

            String htmlContent = buildBatchDepletionContent(firstName, bloodType, depletedBatchNumber, remainingBatches, usageReason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Batch depletion notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send batch depletion notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendWeeklyInventorySummary(String toEmail, String firstName,
                                           Map<String, Object> weeklyStats, List<String> upcomingExpirations) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Weekly Inventory Summary & FIFO Statistics");

            String htmlContent = buildWeeklyInventorySummaryContent(firstName, weeklyStats, upcomingExpirations);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Weekly inventory summary sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send weekly inventory summary to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendDonationRequestToDonor(String toEmail, String firstName, String bloodType,
                                           String urgencyLevel, Integer estimatedShortage, String donationCenter) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your " + bloodType + " Blood is Urgently Needed");

            String htmlContent = buildDonationRequestContent(firstName, bloodType, urgencyLevel, estimatedShortage, donationCenter);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Donation request sent to donor: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send donation request to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendDonationThankYou(String toEmail, String firstName, String bloodType,
                                     String donationDate, String impactMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Thank You for Your Life-Saving Donation");

            String htmlContent = buildDonationThankYouContent(firstName, bloodType, donationDate, impactMessage);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Donation thank you sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send donation thank you to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendSystemMaintenanceNotification(String toEmail, String firstName,
                                                  String maintenanceStartTime, String estimatedDuration, List<String> affectedFeatures) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("System Maintenance Scheduled");

            String htmlContent = buildSystemMaintenanceContent(firstName, maintenanceStartTime, estimatedDuration, affectedFeatures);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("System maintenance notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send system maintenance notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendEmergencyShortageAlert(String toEmail, String firstName, List<String> criticalBloodTypes,
                                           String emergencyContactInfo, List<String> immediateActions) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("EMERGENCY BLOOD SHORTAGE ALERT");

            String htmlContent = buildEmergencyShortageAlertContent(firstName, criticalBloodTypes, emergencyContactInfo, immediateActions);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Emergency shortage alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send emergency shortage alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendMonthlyPerformanceReport(String toEmail, String firstName, String reportPeriod,
                                             Map<String, Object> performanceMetrics, Map<String, Double> fifoEfficiencyStats) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Monthly Performance Report - " + reportPeriod);

            String htmlContent = buildMonthlyPerformanceReportContent(firstName, reportPeriod, performanceMetrics, fifoEfficiencyStats);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Monthly performance report sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send monthly performance report to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendQualityAssuranceAlert(String toEmail, String firstName, String issueType,
                                          List<String> affectedBatches, List<String> immediateActions, Boolean investigationRequired) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Quality Assurance Alert - " + issueType);

            String htmlContent = buildQualityAssuranceAlertContent(firstName, issueType, affectedBatches, immediateActions, investigationRequired);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Quality assurance alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send quality assurance alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendAuditTrailNotification(String toEmail, String firstName, String auditEventType,
                                           String eventDetails, Integer affectedRecords, String complianceStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Audit Trail Notification - " + auditEventType);

            String htmlContent = buildAuditTrailNotificationContent(firstName, auditEventType, eventDetails, affectedRecords, complianceStatus);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Audit trail notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send audit trail notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendTemperatureExcursionAlert(String toEmail, String firstName, String storageLocation,
                                              String temperatureRange, String duration, List<String> affectedInventory) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TEMPERATURE EXCURSION ALERT - " + storageLocation);

            String htmlContent = buildTemperatureExcursionAlertContent(firstName, storageLocation, temperatureRange, duration, affectedInventory);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Temperature excursion alert sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send temperature excursion alert to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ===== EMAIL CONTENT BUILDERS =====

    private String buildEmailContent(String firstName, String lastName, String userType, String password, String toEmail) {
        String roleName = getRoleDisplayName(userType);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .credentials { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .important { color: #dc2626; font-weight: bold; background: #fff5f5; padding: 15px; border-radius: 6px; border: 1px solid #fecaca; }
                .button { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 15px 0; font-weight: 600; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Give Blood, Give Life</h1>
                <p>Blood Donation Management System</p>
                <div class="timestamp">Account Created: %s</div>
            </div>
            <div class="content">
                <h2>Welcome, %s %s!</h2>
                <p>Your account has been successfully created as <strong>%s</strong> in our blood donation management system.</p>
                
                <div class="credentials">
                    <h3>Your Login Credentials</h3>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Temporary Password:</strong> <code style="background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
                </div>

                <div class="important">
                    <strong>Security Notice:</strong> For your account security, please change your password immediately after first login.
                </div>
                
                <center>
                    <a href="http://localhost:8080/login" class="button">Login to Your Account</a>
                </center>
                
                <div class="footer">
                    <p>Thank you for joining our life-saving mission! Together, we can make a difference.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName, lastName, roleName, toEmail, password);
    }

    private String buildBloodRequestNotificationContent(String firstName, Long requestId, String bloodType,
                                                        Integer quantity, String urgency, String hospitalName) {
        String urgencyColor = getUrgencyColor(urgency);
        String urgencyIcon = getUrgencyIcon(urgency);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: %s; color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .request-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid %s; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .button { background: %s; color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; font-weight: 600; }
                .urgency-badge { background: %s; color: white; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>%s New Blood Request</h1>
                <p>Immediate attention required</p>
                <div class="timestamp">Received: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>A new blood request has been submitted and requires your immediate review.</p>
                
                <div class="request-details">
                    <h3>Request Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Request ID:</strong></td><td style="padding: 8px 0;">#%d</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: %s; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Urgency:</strong></td><td style="padding: 8px 0;"><span class="urgency-badge">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Hospital:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                    </table>
                </div>

                <p>Please review this request promptly and take appropriate action through the FIFO system.</p>
                
                <center>
                    <a href="http://localhost:8080/medical-staff/dashboard" class="button">Review Request</a>
                </center>
                
                <div class="footer">
                    <p>Thank you for your prompt attention to this matter.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(urgencyColor, urgencyColor, urgencyColor, urgencyColor, urgencyIcon, currentTime,
                firstName, requestId, urgencyColor, bloodType, quantity, urgency, hospitalName);
    }

    private String buildFulfillmentNotificationContent(String firstName, Long requestId, String bloodType,
                                                       Integer quantity, String fulfilledBy) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .request-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #8b5cf6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .fifo-info { background: #f0f9ff; border: 1px solid #bfdbfe; padding: 15px; border-radius: 6px; margin: 15px 0; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Blood Request Fulfilled</h1>
                <p>Request #%d completed using FIFO system</p>
                <div class="timestamp">Fulfilled: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Your blood request has been successfully fulfilled using our advanced FIFO inventory management system.</p>
                
                <div class="request-details">
                    <h3>Fulfilled Request Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Request ID:</strong></td><td style="padding: 8px 0;">#%d</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #8b5cf6; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Fulfilled By:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold;">FULFILLED</span></td></tr>
                    </table>
                </div>

                <div class="fifo-info">
                    <h4 style="margin-top: 0; color: #1e40af;">FIFO System Benefits:</h4>
                    <ul style="margin-bottom: 0;">
                        <li>Blood closest to expiration was used first</li>
                        <li>Optimal inventory management and minimal waste</li>
                        <li>Automated batch selection for maximum efficiency</li>
                    </ul>
                </div>

                <p>The blood units have been prepared for delivery. Please ensure someone is available to receive the shipment.</p>
                
                <div class="footer">
                    <p>Thank you for using our advanced blood donation management system.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(requestId, currentTime, firstName, requestId, bloodType, quantity, fulfilledBy);
    }

    private String buildBloodUsageNotificationContent(String firstName, String bloodType, Integer quantity,
                                                      String reason, String usedBy) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #0ea5e9, #0369a1); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .usage-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #0ea5e9; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Blood Usage Notification</h1>
                <p>%s blood inventory updated</p>
                <div class="timestamp">Used: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Blood inventory has been updated due to usage processed through our FIFO system.</p>
                
                <div class="usage-details">
                    <h3>Usage Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #0ea5e9; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity Used:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Processed By:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>System:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold;">FIFO Automated</span></td></tr>
                    </table>
                </div>

                <p>The FIFO system automatically selected blood units closest to expiration, ensuring optimal inventory management.</p>
                
                <div class="footer">
                    <p>This notification helps maintain transparency in our blood management system.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(bloodType, currentTime, firstName, bloodType, quantity, reason, usedBy);
    }

    private String buildLowInventoryAlertContent(String firstName, String bloodType, Integer currentStock, Integer threshold) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .alert-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #f59e0b; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .warning { background: #fffbeb; border: 1px solid #fbbf24; padding: 15px; border-radius: 6px; margin: 15px 0; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>LOW INVENTORY ALERT</h1>
                <p>%s blood levels critically low</p>
                <div class="timestamp">Alert Generated: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>This is an urgent notification that %s blood inventory has fallen below the minimum threshold.</p>
                
                <div class="alert-details">
                    <h3>Inventory Status</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #f59e0b; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Current Stock:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Minimum Threshold:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold;">CRITICAL LOW</span></td></tr>
                    </table>
                </div>

                <div class="warning">
                    <strong>Action Required:</strong> Please coordinate with blood donation centers to replenish inventory as soon as possible.
                </div>
                
                <div class="footer">
                    <p>Immediate action is recommended to maintain adequate blood supply.</p>
                    <p><em>This is an automated alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(bloodType, currentTime, firstName, bloodType, bloodType, currentStock, threshold);
    }

    private String buildExpiredBloodAlertContent(String firstName, String bloodType, String batchNumber, String expirationDate) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; animation: pulse 2s infinite; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .alert-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .danger { background: #fef2f2; border: 1px solid #fca5a5; padding: 15px; border-radius: 6px; margin: 15px 0; color: #dc2626; }
                .timestamp { color: #9ca3af; font-size: 12px; }
                @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.8; } 100% { opacity: 1; } }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>EXPIRED BLOOD ALERT</h1>
                <p>Immediate removal required</p>
                <div class="timestamp">Alert Generated: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>This is a critical alert regarding expired blood in our inventory that requires immediate attention.</p>
                
                <div class="alert-details">
                    <h3>Expired Blood Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Batch Number:</strong></td><td style="padding: 8px 0;"><code style="background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Expiration Date:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold;">EXPIRED - REMOVE IMMEDIATELY</span></td></tr>
                    </table>
                </div>

                <div class="danger">
                    <strong>URGENT ACTION REQUIRED:</strong> This expired blood must be removed from inventory immediately and disposed of according to medical waste protocols.
                </div>
                
                <div class="footer">
                    <p>Please acknowledge this alert once the expired blood has been properly disposed of.</p>
                    <p><em>This is an automated alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName, bloodType, batchNumber, expirationDate);
    }

    private String buildFIFOProcessingNotificationContent(String firstName, String bloodType, Integer totalUsed,
                                                          String oldestBatchUsed, String reason) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .fifo-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #10b981; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .success { background: #f0fdf4; border: 1px solid #bbf7d0; padding: 15px; border-radius: 6px; margin: 15px 0; color: #166534; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>FIFO Processing Complete</h1>
                <p>Optimal inventory management executed</p>
                <div class="timestamp">Processed: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Our FIFO (First In, First Out) system has successfully processed a blood usage request with optimal inventory management.</p>
                
                <div class="fifo-details">
                    <h3>FIFO Processing Summary</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Total Units Used:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Oldest Batch Used:</strong></td><td style="padding: 8px 0;"><code style="background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>System:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold;">FIFO Automated</span></td></tr>
                    </table>
                </div>

                <div class="success">
                    <strong>Optimization Benefits:</strong> The FIFO system ensured that blood closest to expiration was used first, minimizing waste and maintaining optimal inventory rotation.
                </div>
                
                <div class="footer">
                    <p>This automated process helps maintain the highest standards of blood inventory management.</p>
                    <p><em>This is an automated notification from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName, bloodType, totalUsed, oldestBatchUsed, reason);
    }

    private String buildInventoryReplenishmentContent(String firstName, String bloodType, Integer addedQuantity, String batchNumber) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .replenishment-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #3b82f6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .info { background: #eff6ff; border: 1px solid #bfdbfe; padding: 15px; border-radius: 6px; margin: 15px 0; color: #1e40af; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Inventory Replenished</h1>
                <p>%s blood added to inventory</p>
                <div class="timestamp">Added: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Blood inventory has been successfully replenished with new units added to our FIFO management system.</p>
                
                <div class="replenishment-details">
                    <h3>Replenishment Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #3b82f6; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity Added:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>New Batch Number:</strong></td><td style="padding: 8px 0;"><code style="background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold;">SUCCESSFULLY ADDED</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>System:</strong></td><td style="padding: 8px 0;"><span style="color: #3b82f6; font-weight: bold;">FIFO Queue Updated</span></td></tr>
                    </table>
                </div>

                <div class="info">
                    <strong>FIFO Integration:</strong> The new blood units have been automatically integrated into our FIFO system and will be used after older inventory based on expiration dates.
                </div>
                
                <div class="footer">
                    <p>Thank you for maintaining our blood inventory supply.</p>
                    <p><em>This is an automated notification from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(bloodType, currentTime, firstName, bloodType, addedQuantity, batchNumber);
    }

    // Continue with existing approval and rejection content builders...

    private String buildApprovalNotificationContent(String firstName, Long requestId, String bloodType,
                                                    Integer quantity, String approvedBy) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .request-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #10b981; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Blood Request Approved</h1>
                <p>Request #%d has been approved</p>
                <div class="timestamp">Approved: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Your blood request has been approved and is now ready for FIFO fulfillment processing.</p>
                
                <div class="request-details">
                    <h3>Approved Request Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Request ID:</strong></td><td style="padding: 8px 0;">#%d</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Approved By:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #10b981; font-weight: bold;">APPROVED</span></td></tr>
                    </table>
                </div>

                <p>The blood units will be prepared for delivery using our FIFO system. Please ensure someone is available to receive the shipment.</p>
                
                <div class="footer">
                    <p>Thank you for using our blood donation system.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(requestId, currentTime, firstName, requestId, bloodType, quantity, approvedBy);
    }

    private String buildRejectionNotificationContent(String firstName, Long requestId, String bloodType,
                                                     Integer quantity, String rejectionReason) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .request-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Blood Request Rejected</h1>
                <p>Request #%d could not be approved</p>
                <div class="timestamp">Rejected: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>We regret to inform you that your blood request could not be approved at this time.</p>
                
                <div class="request-details">
                    <h3>Request Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Request ID:</strong></td><td style="padding: 8px 0;">#%d</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold; font-size: 18px;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Quantity:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold;">REJECTED</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                    </table>
                </div>

                <p>You may submit a new request or contact the medical staff for alternative solutions.</p>
                
                <div class="footer">
                    <p>We apologize for any inconvenience.</p>
                    <p><em>This is an automated message from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(requestId, currentTime, firstName, requestId, bloodType, quantity,
                rejectionReason != null ? rejectionReason : "Not specified");
    }

    // ===== NEW CONTENT BUILDERS FOR MISSING METHODS =====

    private String buildCriticalShortageAlertContent(String firstName, Map<String, Integer> criticalBloodTypes, Integer totalAffectedRequests) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder bloodTypesList = new StringBuilder();
        for (Map.Entry<String, Integer> entry : criticalBloodTypes.entrySet()) {
            bloodTypesList.append("<li>").append(entry.getKey()).append(": ").append(entry.getValue()).append(" units remaining</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #7f1d1d); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; animation: pulse 2s infinite; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .critical-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .urgent { background: #fef2f2; border: 1px solid #fca5a5; padding: 15px; border-radius: 6px; margin: 15px 0; color: #dc2626; }
                .timestamp { color: #9ca3af; font-size: 12px; }
                @keyframes pulse { 0%% { opacity: 1; } 50%% { opacity: 0.8; } 100%% { opacity: 1; } }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>CRITICAL SHORTAGE ALERT</h1>
                <p>Multiple blood types critically low</p>
                <div class="timestamp">Alert Generated: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>This is an urgent alert regarding critical shortages affecting multiple blood types in our inventory.</p>
                
                <div class="critical-details">
                    <h3>Critical Blood Types</h3>
                    <ul>%s</ul>
                    <p><strong>Total Affected Requests:</strong> %d pending requests</p>
                </div>

                <div class="urgent">
                    <strong>IMMEDIATE ACTION REQUIRED:</strong> Contact all donation centers and activate emergency protocols to replenish critical blood supplies.
                </div>
                
                <div class="footer">
                    <p>This is a system-wide critical alert requiring immediate attention.</p>
                    <p><em>This is an automated alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName, bloodTypesList.toString(), totalAffectedRequests);
    }

    private String buildBatchDepletionContent(String firstName, String bloodType, String depletedBatchNumber, Integer remainingBatches, String usageReason) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .depletion-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #f59e0b; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Batch Depleted</h1>
                <p>%s batch %s completely used</p>
                <div class="timestamp">Depleted: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>A blood batch has been completely depleted through FIFO processing.</p>
                
                <div class="depletion-details">
                    <h3>Depletion Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type:</strong></td><td style="padding: 8px 0;"><span style="color: #f59e0b; font-weight: bold;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Depleted Batch:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Remaining Batches:</strong></td><td style="padding: 8px 0;">%d</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Usage Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                    </table>
                </div>
                
                <div class="footer">
                    <p>FIFO system automatically moved to next oldest batch.</p>
                    <p><em>This is an automated notification from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(bloodType, depletedBatchNumber, currentTime, firstName, bloodType, depletedBatchNumber, remainingBatches, usageReason);
    }

    private String buildWeeklyInventorySummaryContent(String firstName, Map<String, Object> weeklyStats, List<String> upcomingExpirations) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder expirationsList = new StringBuilder();
        for (String expiration : upcomingExpirations) {
            expirationsList.append("<li>").append(expiration).append("</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #3b82f6, #1e40af); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .stats-section { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #3b82f6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Weekly Inventory Summary</h1>
                <p>FIFO system performance & statistics</p>
                <div class="timestamp">Generated: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Here's your weekly inventory summary with FIFO system performance metrics.</p>
                
                <div class="stats-section">
                    <h3>Weekly Statistics</h3>
                    <p><strong>Total Requests Processed:</strong> %s</p>
                    <p><strong>FIFO Efficiency:</strong> %s%%</p>
                    <p><strong>Waste Reduction:</strong> %s%%</p>
                </div>

                <div class="stats-section">
                    <h3>Upcoming Expirations</h3>
                    <ul>%s</ul>
                </div>
                
                <div class="footer">
                    <p>Weekly reports help optimize inventory management.</p>
                    <p><em>This is an automated report from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName,
                weeklyStats.getOrDefault("totalRequests", "N/A"),
                weeklyStats.getOrDefault("fifoEfficiency", "N/A"),
                weeklyStats.getOrDefault("wasteReduction", "N/A"),
                expirationsList.toString());
    }

    private String buildDonationRequestContent(String firstName, String bloodType, String urgencyLevel, Integer estimatedShortage, String donationCenter) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .donation-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .button { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; font-weight: 600; }
                .timestamp { color: #9ca3af; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Your Blood is Needed!</h1>
                <p>%s blood urgently required</p>
                <div class="timestamp">Request sent: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>We urgently need your %s blood donation to help save lives in our community.</p>
                
                <div class="donation-info">
                    <h3>Donation Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Blood Type Needed:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Urgency Level:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Estimated Shortage:</strong></td><td style="padding: 8px 0;">%d units</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Donation Center:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                    </table>
                </div>

                <p>Your donation can make the difference between life and death for patients in need.</p>
                
                <center>
                    <a href="http://localhost:8080/donor/schedule" class="button">Schedule Donation</a>
                </center>
                
                <div class="footer">
                    <p>Thank you for being a life-saver in our community.</p>
                    <p><em>This is a request from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(bloodType, currentTime, firstName, bloodType, bloodType, urgencyLevel, estimatedShortage, donationCenter);
    }

    private String buildDonationThankYouContent(String firstName, String bloodType, String donationDate, String impactMessage) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .thank-you { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #10b981; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Thank You, Hero!</h1>
                <p>Your donation saves lives</p>
            </div>
            <div class="content">
                <h2>Dear %s,</h2>
                <p>Thank you for your generous %s blood donation on %s.</p>
                
                <div class="thank-you">
                    <h3>Your Impact</h3>
                    <p>%s</p>
                </div>
                
                <div class="footer">
                    <p>You are a true hero in our community!</p>
                    <p><em>With gratitude from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, bloodType, donationDate, impactMessage);
    }

    private String buildSystemMaintenanceContent(String firstName, String maintenanceStartTime, String estimatedDuration, List<String> affectedFeatures) {
        StringBuilder featuresList = new StringBuilder();
        for (String feature : affectedFeatures) {
            featuresList.append("<li>").append(feature).append("</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #6b7280, #4b5563); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .maintenance-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #6b7280; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>System Maintenance Scheduled</h1>
                <p>Temporary service interruption</p>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>We have scheduled system maintenance to improve our services.</p>
                
                <div class="maintenance-info">
                    <h3>Maintenance Details</h3>
                    <p><strong>Start Time:</strong> %s</p>
                    <p><strong>Estimated Duration:</strong> %s</p>
                    <h4>Affected Features:</h4>
                    <ul>%s</ul>
                </div>
                
                <div class="footer">
                    <p>We apologize for any inconvenience.</p>
                    <p><em>This is a notification from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, maintenanceStartTime, estimatedDuration, featuresList.toString());
    }

    private String buildEmergencyShortageAlertContent(String firstName, List<String> criticalBloodTypes, String emergencyContactInfo, List<String> immediateActions) {
        StringBuilder bloodTypesList = new StringBuilder();
        for (String bloodType : criticalBloodTypes) {
            bloodTypesList.append("<li>").append(bloodType).append("</li>");
        }

        StringBuilder actionsList = new StringBuilder();
        for (String action : immediateActions) {
            actionsList.append("<li>").append(action).append("</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #7f1d1d); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; animation: pulse 2s infinite; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .emergency-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                @keyframes pulse { 0%% { opacity: 1; } 50%% { opacity: 0.8; } 100%% { opacity: 1; } }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>EMERGENCY SHORTAGE ALERT</h1>
                <p>Critical blood shortage - immediate action required</p>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>This is an emergency alert regarding critical blood shortages.</p>
                
                <div class="emergency-info">
                    <h3>Critical Blood Types:</h3>
                    <ul>%s</ul>
                    <h3>Emergency Contact:</h3>
                    <p>%s</p>
                    <h3>Immediate Actions Required:</h3>
                    <ul>%s</ul>
                </div>
                
                <div class="footer">
                    <p>This is a system-wide emergency alert.</p>
                    <p><em>This is an automated emergency alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, bloodTypesList.toString(), emergencyContactInfo, actionsList.toString());
    }

    private String buildMonthlyPerformanceReportContent(String firstName, String reportPeriod, Map<String, Object> performanceMetrics, Map<String, Double> fifoEfficiencyStats) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .report-section { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #8b5cf6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Monthly Performance Report</h1>
                <p>Period: %s</p>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>Here's your monthly performance report with detailed metrics and FIFO system efficiency.</p>
                
                <div class="report-section">
                    <h3>Performance Metrics</h3>
                    <p><strong>Total Donations:</strong> %s</p>
                    <p><strong>Requests Fulfilled:</strong> %s</p>
                    <p><strong>Average Response Time:</strong> %s hours</p>
                </div>

                <div class="report-section">
                    <h3>FIFO System Efficiency</h3>
                    <p><strong>Waste Reduction:</strong> %.1f%%</p>
                    <p><strong>Optimal Usage Rate:</strong> %.1f%%</p>
                    <p><strong>Inventory Turnover:</strong> %.1f</p>
                </div>
                
                <div class="footer">
                    <p>Monthly reports help track system performance and improvements.</p>
                    <p><em>This is an automated report from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(reportPeriod, firstName,
                performanceMetrics.getOrDefault("totalDonations", "N/A"),
                performanceMetrics.getOrDefault("requestsFulfilled", "N/A"),
                performanceMetrics.getOrDefault("avgResponseTime", "N/A"),
                fifoEfficiencyStats.getOrDefault("wasteReduction", 0.0),
                fifoEfficiencyStats.getOrDefault("optimalUsage", 0.0),
                fifoEfficiencyStats.getOrDefault("inventoryTurnover", 0.0));
    }

    private String buildQualityAssuranceAlertContent(String firstName, String issueType, List<String> affectedBatches, List<String> immediateActions, Boolean investigationRequired) {
        StringBuilder batchesList = new StringBuilder();
        for (String batch : affectedBatches) {
            batchesList.append("<li>").append(batch).append("</li>");
        }

        StringBuilder actionsList = new StringBuilder();
        for (String action : immediateActions) {
            actionsList.append("<li>").append(action).append("</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .qa-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #f59e0b; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Quality Assurance Alert</h1>
                <p>Issue Type: %s</p>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>A quality assurance issue has been detected that requires your attention.</p>
                
                <div class="qa-info">
                    <h3>Affected Batches:</h3>
                    <ul>%s</ul>
                    <h3>Immediate Actions Required:</h3>
                    <ul>%s</ul>
                    <p><strong>Investigation Required:</strong> %s</p>
                </div>
                
                <div class="footer">
                    <p>Quality assurance is critical for patient safety.</p>
                    <p><em>This is an automated QA alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(issueType, firstName, batchesList.toString(), actionsList.toString(), investigationRequired ? "Yes" : "No");
    }

    private String buildAuditTrailNotificationContent(String firstName, String auditEventType, String eventDetails, Integer affectedRecords, String complianceStatus) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #6366f1, #4f46e5); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .audit-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #6366f1; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Audit Trail Notification</h1>
                <p>Event: %s</p>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>An audit event has been recorded in the system.</p>
                
                <div class="audit-info">
                    <h3>Audit Details</h3>
                    <p><strong>Event Type:</strong> %s</p>
                    <p><strong>Details:</strong> %s</p>
                    <p><strong>Affected Records:</strong> %d</p>
                    <p><strong>Compliance Status:</strong> %s</p>
                </div>
                
                <div class="footer">
                    <p>Audit trails ensure system transparency and compliance.</p>
                    <p><em>This is an automated audit notification from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(auditEventType, firstName, auditEventType, eventDetails, affectedRecords, complianceStatus);
    }

    private String buildTemperatureExcursionAlertContent(String firstName, String storageLocation, String temperatureRange, String duration, List<String> affectedInventory) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder inventoryList = new StringBuilder();
        for (String inventory : affectedInventory) {
            inventoryList.append("<li>").append(inventory).append("</li>");
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #dc2626, #7f1d1d); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; animation: pulse 2s infinite; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
                .temp-alert { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #dc2626; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                .critical { background: #fef2f2; border: 1px solid #fca5a5; padding: 15px; border-radius: 6px; margin: 15px 0; color: #dc2626; }
                .timestamp { color: #9ca3af; font-size: 12px; }
                @keyframes pulse { 0%% { opacity: 1; } 50%% { opacity: 0.8; } 100%% { opacity: 1; } }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>TEMPERATURE EXCURSION ALERT</h1>
                <p>Critical storage temperature breach</p>
                <div class="timestamp">Alert Generated: %s</div>
            </div>
            <div class="content">
                <h2>Hello, %s!</h2>
                <p>A critical temperature excursion has been detected in blood storage that requires immediate attention.</p>
                
                <div class="temp-alert">
                    <h3>Temperature Excursion Details</h3>
                    <table style="width: 100%%; border-spacing: 0;">
                        <tr><td style="padding: 8px 0;"><strong>Storage Location:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Temperature Range:</strong></td><td style="padding: 8px 0;"><span style="color: #dc2626; font-weight: bold;">%s</span></td></tr>
                        <tr><td style="padding: 8px 0;"><strong>Duration:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                    </table>
                    <h4>Affected Inventory:</h4>
                    <ul>%s</ul>
                </div>

                <div class="critical">
                    <strong>CRITICAL ACTION REQUIRED:</strong> Immediately assess affected blood units for viability and quarantine if necessary. Document all actions taken for compliance records.
                </div>
                
                <div class="footer">
                    <p>Temperature control is critical for blood safety and patient health.</p>
                    <p><em>This is an automated critical alert from the Blood Donation Management System.</em></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(currentTime, firstName, storageLocation, temperatureRange, duration, inventoryList.toString());
    }

    // ===== HELPER METHODS =====

    private String getRoleDisplayName(String userType) {
        return switch (userType) {
            case "MEDICAL_STAFF" -> "Medical Staff";
            case "HOSPITAL_STAFF" -> "Hospital Staff";
            case "REGIONAL_LIAISON" -> "Regional Liaison";
            case "DONOR" -> "Blood Donor";
            case "SYSTEM_ADMIN" -> "System Administrator";
            default -> userType;
        };
    }

    private String getUrgencyColor(String urgency) {
        return switch (urgency) {
            case "CRITICAL" -> "#dc2626";
            case "HIGH" -> "#f59e0b";
            case "MEDIUM" -> "#3b82f6";
            case "LOW" -> "#10b981";
            default -> "#6b7280";
        };
    }

    private String getUrgencyIcon(String urgency) {
        return switch (urgency) {
            case "CRITICAL" -> "CRITICAL";
            case "HIGH" -> "HIGH";
            case "MEDIUM" -> "MEDIUM";
            case "LOW" -> "LOW";
            default -> "";
        };
    }

    @Override
    public void sendEmailChangeConfirmation(String toEmail, String firstName, String lastName,
                                            String userType, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Address Updated - Give Blood, Give Life");

            String htmlContent = buildEmailChangeConfirmationContent(firstName, lastName, userType, temporaryPassword, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email change confirmation sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send email change confirmation to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email change confirmation", e);
        }
    }

    private String buildEmailChangeConfirmationContent(String firstName, String lastName,
                                                       String userType, String password, String newEmail) {
        String roleName = getRoleDisplayName(userType);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
            .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
            .credentials { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #3b82f6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            .important { color: #dc2626; font-weight: bold; background: #fff5f5; padding: 15px; border-radius: 6px; border: 1px solid #fecaca; }
            .button { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 15px 0; font-weight: 600; }
            .timestamp { color: #9ca3af; font-size: 12px; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>Email Address Updated</h1>
            <p>Give Blood, Give Life Account</p>
            <div class="timestamp">Updated: %s</div>
        </div>
        <div class="content">
            <h2>Hello, %s %s!</h2>
            <p>Your email address has been successfully updated as requested.</p>
            
            <div class="credentials">
                <h3>Your Updated Login Credentials</h3>
                <p><strong>New Email:</strong> %s</p>
                <p><strong>Temporary Password:</strong> <code style="background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-family: monospace;">%s</code></p>
            </div>

            <div class="important">
                <strong>Security Notice:</strong> For your account security, please change your password immediately after first login with your new email address.
            </div>
            
            <center>
                <a href="http://localhost:8080/login" class="button">Login with New Email</a>
            </center>
            
            <div class="footer">
                <p>If you did not request this change, please contact our support team immediately.</p>
                <p><em>This is an automated message from the Blood Donation Management System.</em></p>
            </div>
        </div>
    </body>
    </html>
    """.formatted(currentTime, firstName, lastName, newEmail, password);
    }

    // Add this method to EmailServiceImpl.java
    @Override
    public void sendEmailChangeCancellationNotification(String toEmail, String firstName, String lastName,
                                                        Long requestId, String cancellationReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Change Request Cancelled - Request #" + requestId);

            String htmlContent = buildEmailChangeCancellationContent(firstName, lastName, requestId, cancellationReason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email change cancellation notification sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send email change cancellation notification to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email change cancellation notification", e);
        }
    }

    private String buildEmailChangeCancellationContent(String firstName, String lastName,
                                                       Long requestId, String cancellationReason) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
            .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
            .cancellation-details { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #f59e0b; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            .info { background: #fffbeb; border: 1px solid #fbbf24; padding: 15px; border-radius: 6px; margin: 15px 0; }
            .timestamp { color: #9ca3af; font-size: 12px; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>Email Change Request Cancelled</h1>
            <p>Request #%d could not be processed</p>
            <div class="timestamp">Cancelled: %s</div>
        </div>
        <div class="content">
            <h2>Hello, %s %s!</h2>
            <p>Your email change request has been reviewed and could not be approved at this time.</p>
            
            <div class="cancellation-details">
                <h3>Request Details</h3>
                <table style="width: 100%%; border-spacing: 0;">
                    <tr><td style="padding: 8px 0;"><strong>Request ID:</strong></td><td style="padding: 8px 0;">#%d</td></tr>
                    <tr><td style="padding: 8px 0;"><strong>Status:</strong></td><td style="padding: 8px 0;"><span style="color: #f59e0b; font-weight: bold;">CANCELLED</span></td></tr>
                    <tr><td style="padding: 8px 0;"><strong>Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                </table>
            </div>

            <div class="info">
                <strong>Next Steps:</strong> You may submit a new email change request with additional information or contact our support team for assistance.
            </div>
            
            <div class="footer">
                <p>If you believe this was a mistake, please contact our support team.</p>
                <p><em>This is an automated message from the Blood Donation Management System.</em></p>
            </div>
        </div>
    </body>
    </html>
    """.formatted(requestId, currentTime, firstName, lastName, requestId,
                cancellationReason != null ? cancellationReason : "No specific reason provided");
    }

    // Add to EmailServiceImpl.java
    @Override
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Give Blood, Give Life");

            String htmlContent = buildPasswordResetEmailContent(firstName, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendNewPasswordEmail(String toEmail, String firstName, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your New Password - Give Blood, Give Life");

            String htmlContent = buildNewPasswordEmailContent(firstName, temporaryPassword);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("New password email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send new password email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send new password email", e);
        }
    }

    private String buildPasswordResetEmailContent(String firstName, String resetLink) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
            .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
            .reset-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #3b82f6; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            .button { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 15px 0; font-weight: 600; }
            .warning { background: #fffbeb; border: 1px solid #fbbf24; padding: 15px; border-radius: 6px; margin: 15px 0; }
            .timestamp { color: #9ca3af; font-size: 12px; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>Password Reset Request</h1>
            <p>Give Blood, Give Life Account</p>
            <div class="timestamp">Requested: %s</div>
        </div>
        <div class="content">
            <h2>Hello, %s!</h2>
            <p>We received a request to reset your password for your Give Blood, Give Life account.</p>
            
            <div class="reset-info">
                <h3>Reset Your Password</h3>
                <p>Click the button below to create a new password. This link will expire in 1 hour for security reasons.</p>
                
                <center>
                    <a href="%s" class="button">Reset Your Password</a>
                </center>
                
                <p style="text-align: center; margin-top: 10px; font-size: 14px; color: #6b7280;">
                    Or copy and paste this link in your browser:<br>
                    <code style="background: #f3f4f6; padding: 8px; border-radius: 4px; word-break: break-all;">%s</code>
                </p>
            </div>

            <div class="warning">
                <strong>Security Notice:</strong> If you didn't request this password reset, please ignore this email. Your account remains secure.
            </div>
            
            <div class="footer">
                <p>This is an automated message from the Blood Donation Management System.</p>
            </div>
        </div>
    </body>
    </html>
    """.formatted(currentTime, firstName, resetLink, resetLink);
    }

    private String buildNewPasswordEmailContent(String firstName, String temporaryPassword) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center; border-radius: 12px 12px 0 0; }
            .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px; }
            .password-info { background: white; padding: 25px; border-radius: 8px; border-left: 4px solid #10b981; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
            .important { color: #dc2626; font-weight: bold; background: #fff5f5; padding: 15px; border-radius: 6px; border: 1px solid #fecaca; }
            .button { background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 15px 0; font-weight: 600; }
            .timestamp { color: #9ca3af; font-size: 12px; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>Password Reset Successful</h1>
            <p>Your new password has been generated</p>
            <div class="timestamp">Reset: %s</div>
        </div>
        <div class="content">
            <h2>Hello, %s!</h2>
            <p>Your password has been successfully reset. Here are your new login credentials:</p>
            
            <div class="password-info">
                <h3>Your New Temporary Password</h3>
                <p><strong>Temporary Password:</strong> <code style="background: #f3f4f6; padding: 8px 12px; border-radius: 4px; font-family: monospace; font-size: 16px; letter-spacing: 1px;">%s</code></p>
            </div>

            <div class="important">
                <strong>Security Notice:</strong> For your account security, please change your password immediately after logging in with this temporary password.
            </div>
            
            <center>
                <a href="http://localhost:8080/login" class="button">Login to Your Account</a>
            </center>
            
            <div class="footer">
                <p>If you didn't request this password reset, please contact our support team immediately.</p>
                <p><em>This is an automated message from the Blood Donation Management System.</em></p>
            </div>
        </div>
    </body>
    </html>
    """.formatted(currentTime, firstName, temporaryPassword);
    }
}