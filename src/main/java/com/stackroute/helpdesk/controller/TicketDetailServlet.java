package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.CommentService;
import com.stackroute.helpdesk.service.FeedbackService;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.CommentServiceImpl;
import com.stackroute.helpdesk.service.impl.FeedbackServiceImpl;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/ticket-detail")
public class TicketDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService = new TicketServiceImpl();
    private final CommentService commentService = new CommentServiceImpl();
    private final FeedbackService feedbackService = new FeedbackServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        try {
            int ticketId = Integer.parseInt(idStr.trim());
            Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);

            if (ticketOpt.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/tickets?error=not_found");
                return;
            }

            Ticket ticket = ticketOpt.get();

            HttpSession session = request.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

            // Strict ticket privacy: Regular users cannot view other users' tickets
            if (currentUser != null && !currentUser.isTechnician() && ticket.getUserId() != currentUser.getUserId()) {
                response.sendRedirect(request.getContextPath() + "/tickets?error=unauthorized_ticket");
                return;
            }

            List<TicketComment> comments = commentService.getCommentsForTicket(ticketId);
            Optional<TicketFeedback> feedbackOpt = feedbackService.getFeedbackForTicket(ticketId);

            request.setAttribute("ticket", ticket);
            request.setAttribute("comments", comments);
            feedbackOpt.ifPresent(fb -> request.setAttribute("feedback", fb));

            request.getRequestDispatcher("/ticket-detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tickets");
        }
    }
}