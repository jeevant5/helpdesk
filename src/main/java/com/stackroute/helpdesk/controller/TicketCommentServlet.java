package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.CommentService;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.CommentServiceImpl;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/ticket-comment")
public class TicketCommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CommentService commentService;
    private final TicketService ticketService;

    public TicketCommentServlet() {
        this(new CommentServiceImpl(), new TicketServiceImpl());
    }

    public TicketCommentServlet(CommentService commentService, TicketService ticketService) {
        this.commentService = commentService;
        this.ticketService = ticketService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String ticketIdStr = request.getParameter("ticketId");
        String commentText = request.getParameter("commentText");
        String newStatus = request.getParameter("newStatus");

        if (ticketIdStr == null || ticketIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());

            // Strict privacy: Regular users can only comment on their own tickets
            if (!currentUser.isTechnician()) {
                Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
                if (ticketOpt.isEmpty() || ticketOpt.get().getUserId() != currentUser.getUserId()) {
                    response.sendRedirect(request.getContextPath() + "/tickets?error=unauthorized_ticket");
                    return;
                }
                // Closed ticket: Users cannot talk in the discussion thread after ticket is closed
                if ("CLOSED".equalsIgnoreCase(ticketOpt.get().getStatus())) {
                    response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&error=ticket_closed");
                    return;
                }
                newStatus = "NO_CHANGE";
            }

            // Administrator oversight rule: Admin cannot resolve tickets
            if (currentUser.isAdmin()) {
                if ("RESOLVED".equalsIgnoreCase(newStatus)) {
                    response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&error=admin_cannot_resolve");
                    return;
                }
                newStatus = "NO_CHANGE";
            }

            TicketComment comment = TicketComment.builder()
                .ticketId(ticketId)
                .authorId(currentUser.getUserId())
                .commentText(commentText)
                .build();

            ServiceResult<TicketComment> result = commentService.addComment(comment, newStatus);
            response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&msg=comment_added");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tickets");
        }
    }
}