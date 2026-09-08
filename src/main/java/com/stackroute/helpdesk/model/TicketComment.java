package com.stackroute.helpdesk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing a discussion or resolution comment in a ticket thread.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketComment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int commentId;
    private int ticketId;
    private int authorId;
    private String commentText;
    private Timestamp createdAt;

    // Joined fields for presentation
    private String authorName;
    private String authorRole;

    public TicketComment(int ticketId, int authorId, String commentText) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.commentText = commentText;
    }
}