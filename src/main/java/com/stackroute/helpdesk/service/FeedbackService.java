package com.stackroute.helpdesk.service;

import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.Optional;

public interface FeedbackService {
    ServiceResult<TicketFeedback> submitFeedback(int ticketId, int rating, String notes, int userId);
    Optional<TicketFeedback> getFeedbackForTicket(int ticketId);
}