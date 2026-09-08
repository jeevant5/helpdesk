package com.stackroute.helpdesk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing Customer Satisfaction (CSAT) rating and review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFeedback implements Serializable {
    private static final long serialVersionUID = 1L;

    private int feedbackId;
    private int ticketId;
    private int rating; // 1 to 5 stars
    private String notes;
    private Timestamp createdAt;

    public TicketFeedback(int ticketId, int rating, String notes) {
        this.ticketId = ticketId;
        this.rating = rating;
        this.notes = notes;
    }
}