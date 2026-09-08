package com.stackroute.helpdesk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing an IT support ticket with SLA resolution tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // Additional display projections for UI table joins
    private String userName;
    private String userEmail;
    private String techName;

    // --- SLA Calculation & Performance Indicators ---
    public int getSlaHours() {
        if ("HIGH".equalsIgnoreCase(this.priority)) return 4;
        if ("MEDIUM".equalsIgnoreCase(this.priority)) return 24;
        return 72; // LOW priority
    }

    public long getSlaDeadlineMillis() {
        if (createdAt == null) return System.currentTimeMillis();
        return createdAt.getTime() + (getSlaHours() * 3600L * 1000L);
    }

    public boolean isSlaBreached() {
        if ("RESOLVED".equalsIgnoreCase(this.status) || "CLOSED".equalsIgnoreCase(this.status)) {
            return false;
        }
        return System.currentTimeMillis() > getSlaDeadlineMillis();
    }

    public String getSlaStatusText() {
        if ("RESOLVED".equalsIgnoreCase(this.status) || "CLOSED".equalsIgnoreCase(this.status)) {
            return "Resolved";
        }
        long now = System.currentTimeMillis();
        long deadline = getSlaDeadlineMillis();
        long diffMillis = Math.abs(deadline - now);

        long hours = diffMillis / (3600L * 1000L);
        long minutes = (diffMillis % (3600L * 1000L)) / (60L * 1000L);

        if (now > deadline) {
            return "SLA Breached (" + hours + "h " + minutes + "m overdue)";
        } else {
            return "SLA: " + hours + "h " + minutes + "m left";
        }
    }

    public String getSlaTimeRemaining() {
        return getSlaStatusText();
    }

    public String getSlaBadgeClass() {
        if ("RESOLVED".equalsIgnoreCase(this.status) || "CLOSED".equalsIgnoreCase(this.status)) {
            return "badge bg-secondary";
        }
        if (isSlaBreached()) {
            return "badge bg-danger";
        }
        long hoursLeft = (getSlaDeadlineMillis() - System.currentTimeMillis()) / (3600L * 1000L);
        if (hoursLeft <= 2) {
            return "badge bg-warning text-dark";
        }
        return "badge bg-success";
    }
}