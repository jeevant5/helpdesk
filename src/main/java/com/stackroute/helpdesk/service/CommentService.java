package com.stackroute.helpdesk.service;

import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.List;

public interface CommentService {
    ServiceResult<TicketComment> addComment(TicketComment comment, String newStatus);
    List<TicketComment> getCommentsForTicket(int ticketId);
}