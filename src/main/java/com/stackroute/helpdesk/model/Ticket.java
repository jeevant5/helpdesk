package com.stackroute.helpdesk.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ticketId;
    private int userId;
    private Integer techId;
    private String title;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH
    private String status;   // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String attachmentName;
    private String attachmentType;
    private boolean hasAttachment;
    private Timestamp createdAt;

    // Additional display fields for UI joining
    private String userName;
    private String userEmail;
    private String techName;

    public Ticket() {}

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getTechId() { return techId; }
    public void setTechId(Integer techId) { this.techId = techId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public boolean isHasAttachment() { return hasAttachment; }
    public void setHasAttachment(boolean hasAttachment) { this.hasAttachment = hasAttachment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTechName() { return techName; }
    public void setTechName(String techName) { this.techName = techName; }
}