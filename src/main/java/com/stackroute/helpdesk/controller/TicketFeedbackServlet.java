package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.FeedbackService;
import com.stackroute.helpdesk.service.impl.FeedbackServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/submit-feedback")
public class TicketFeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final FeedbackService feedbackService = new FeedbackServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String ticketIdStr = request.getParameter("ticketId");
        String ratingStr = request.getParameter("rating");
        String notes = request.getParameter("notes");

        if (ticketIdStr == null || ratingStr == null) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());
            int rating = Integer.parseInt(ratingStr.trim());

            ServiceResult<TicketFeedback> result = feedbackService.submitFeedback(
                ticketId, rating, notes, currentUser.getUserId()
            );

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&feedbackSuccess=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&error=" + result.getMessage());
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tickets");
        }
    }
}