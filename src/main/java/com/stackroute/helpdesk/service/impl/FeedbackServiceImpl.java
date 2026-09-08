package com.stackroute.helpdesk.service.impl;

import com.stackroute.helpdesk.dao.FeedbackDAO;
import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.service.FeedbackService;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.Optional;

public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackDAO feedbackDAO;
    private final TicketDAO ticketDAO;

    public FeedbackServiceImpl() {
        this.feedbackDAO = new FeedbackDAO();
        this.ticketDAO = new TicketDAO();
    }

    public FeedbackServiceImpl(FeedbackDAO feedbackDAO, TicketDAO ticketDAO) {
        this.feedbackDAO = feedbackDAO;
        this.ticketDAO = ticketDAO;
    }

    @Override
    public ServiceResult<TicketFeedback> submitFeedback(int ticketId, int rating, String notes, int userId) {
        if (ticketId <= 0) {
            return ServiceResult.fail("INVALID_ID", "Invalid ticket ID.");
        }
        if (rating < 1 || rating > 5) {
            return ServiceResult.fail("INVALID_RATING", "Rating must be between 1 and 5 stars.");
        }

        Ticket ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null) {
            return ServiceResult.fail("NOT_FOUND", "Ticket #" + ticketId + " was not found.");
        }

        if (ticket.getUserId() != userId) {
            return ServiceResult.fail("UNAUTHORIZED", "Only the ticket author may submit resolution feedback.");
        }

        if (!"RESOLVED".equalsIgnoreCase(ticket.getStatus()) && !"CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            return ServiceResult.fail("INVALID_STATE", "Feedback can only be submitted for RESOLVED or CLOSED tickets.");
        }

        TicketFeedback existing = feedbackDAO.getFeedbackByTicketId(ticketId);
        if (existing != null) {
            return ServiceResult.fail("ALREADY_EXISTS", "Customer feedback has already been submitted for this ticket.");
        }

        TicketFeedback feedback = TicketFeedback.builder()
            .ticketId(ticketId)
            .rating(rating)
            .notes(notes != null ? notes.trim() : null)
            .build();

        boolean saved = feedbackDAO.addFeedback(feedback);
        if (saved) {
            return ServiceResult.ok(feedback, "Thank you! Your feedback has been recorded successfully.");
        }

        return ServiceResult.fail("DB_ERROR", "Could not save feedback due to a database error.");
    }

    @Override
    public Optional<TicketFeedback> getFeedbackForTicket(int ticketId) {
        if (ticketId <= 0) return Optional.empty();
        return Optional.ofNullable(feedbackDAO.getFeedbackByTicketId(ticketId));
    }
}