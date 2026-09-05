package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.CommentDAO;
import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Functional Requirement from specification:
 * Ticket Discussion Thread: TicketCommentServlet handles adding updates to a ticket, 
 * dynamically changing ticket status from OPEN to IN_PROGRESS or RESOLVED.
 */
@WebServlet("/ticket-comment")
public class TicketCommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CommentDAO commentDAO = new CommentDAO();
    private TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        String ticketIdStr = request.getParameter("ticketId");
        String commentText = request.getParameter("commentText");
        String newStatus = request.getParameter("newStatus");

        if (ticketIdStr == null || ticketIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        int ticketId;
        try {
            ticketId = Integer.parseInt(ticketIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        Ticket ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null) {
            response.sendRedirect(request.getContextPath() + "/tickets?error=not_found");
            return;
        }

        // Add discussion update comment if provided
        if (commentText != null && !commentText.trim().isEmpty()) {
            TicketComment comment = new TicketComment();
            comment.setTicketId(ticketId);
            comment.setAuthorId(currentUser.getUserId());
            comment.setCommentText(commentText.trim());
            commentDAO.addComment(comment);
        }

        // Dynamically change ticket status from OPEN to IN_PROGRESS or RESOLVED
        if (newStatus != null && !newStatus.trim().isEmpty() && !newStatus.equalsIgnoreCase("NO_CHANGE")) {
            newStatus = newStatus.trim().toUpperCase();
            if (newStatus.equals("IN_PROGRESS") || newStatus.equals("RESOLVED") || newStatus.equals("OPEN") || newStatus.equals("CLOSED")) {
                ticketDAO.updateStatus(ticketId, newStatus);
                // If technician updates status and ticket has no technician yet, assign to current tech
                if (currentUser.isTechnician() && (ticket.getTechId() == null || ticket.getTechId() == 0)) {
                    ticketDAO.assignTicket(ticketId, currentUser.getUserId());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&msg=updated");
    }
}