package com.bloodyy.Blood.Donation.web.based.System.dto;

public class FeedbackDTO {
    private String content;
    private Long feedbackId;
    private String feedbackType; // "GENERAL", "MEDICAL", "HOSPITAL"

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
}