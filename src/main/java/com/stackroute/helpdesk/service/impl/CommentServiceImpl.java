package com.stackroute.helpdesk.service.impl;

import com.stackroute.helpdesk.dao.CommentDAO;
import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.service.CommentService;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.Collections;
import java.util.List;

public class CommentServiceImpl implements CommentService {

    private final CommentDAO commentDAO;
    private final TicketDAO ticketDAO;

    public CommentServiceImpl() {
        this.commentDAO = new CommentDAO();
        this.ticketDAO = new TicketDAO();
    }

    public CommentServiceImpl(CommentDAO commentDAO, TicketDAO ticketDAO) {
        this.commentDAO = commentDAO;
        this.ticketDAO = ticketDAO;
    }

    @Override
    public ServiceResult<TicketComment> addComment(TicketComment comment, String newStatus) {
        if (comment == null || comment.getTicketId() <= 0) {
            return ServiceResult.fail("VALIDATION_ERROR", "Valid ticket identifier is required.");
        }
        if (comment.getCommentText() == null || comment.getCommentText().isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Comment text cannot be empty.");
        }

        boolean saved = commentDAO.addComment(comment);
        if (!saved) {
            return ServiceResult.fail("DB_ERROR", "Could not save comment to the discussion thread.");
        }

        // Dynamically transition ticket status if requested
        if (newStatus != null && !newStatus.isBlank() && !"NO_CHANGE".equalsIgnoreCase(newStatus)) {
            ticketDAO.updateStatus(comment.getTicketId(), newStatus.trim().toUpperCase());
        }

        return ServiceResult.ok(comment, "Comment posted successfully.");
    }

    @Override
    public List<TicketComment> getCommentsForTicket(int ticketId) {
        if (ticketId <= 0) return Collections.emptyList();
        return commentDAO.getCommentsByTicketId(ticketId);
    }
}