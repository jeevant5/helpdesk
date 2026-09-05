package com.stackroute.helpdesk.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class TicketComment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int commentId;
    private int ticketId;
    private int authorId;
    private String commentText;
    private Timestamp createdAt;

    // Joined fields for display
    private String authorName;
    private String authorRole;

    public TicketComment() {}

    public TicketComment(int ticketId, int authorId, String commentText) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.commentText = commentText;
    }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
}