package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/assign-ticket")
public class TicketAssignServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService;

    public TicketAssignServlet() {
        this(new TicketServiceImpl());
    }

    public TicketAssignServlet(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !currentUser.isTechnician()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: Only technicians and administrators can assign tickets.");
            return;
        }

        String ticketIdStr = request.getParameter("ticketId");
        if (ticketIdStr == null || ticketIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tech-dashboard?error=invalid_id");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());
            int techId = currentUser.getUserId();

            // Admin can assign tickets to any technician specified by techId parameter
            if (currentUser.isAdmin()) {
                String techIdStr = request.getParameter("techId");
                if (techIdStr != null && !techIdStr.isBlank()) {
                    techId = Integer.parseInt(techIdStr.trim());
                }
            }

            ServiceResult<Void> result = ticketService.assignTicket(ticketId, techId);

            String redirect = request.getParameter("redirect");
            if ("detail".equalsIgnoreCase(redirect)) {
                response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&msg=assigned");
            } else {
                response.sendRedirect(request.getContextPath() + "/tech-dashboard?msg=assigned");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tech-dashboard?error=invalid_id");
        }
    }
}