package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.User;

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
        if (!currentUser.isTechnician()) {
            response.sendRedirect(request.getContextPath() + "/tickets?error=unauthorized");
            return;
        }

        String ticketIdStr = request.getParameter("ticketId");
        if (ticketIdStr != null && !ticketIdStr.trim().isEmpty()) {
            try {
                int ticketId = Integer.parseInt(ticketIdStr.trim());
                // Technicians assign tickets to themselves
                boolean success = ticketDAO.assignTicket(ticketId, currentUser.getUserId());
                String redirect = request.getParameter("redirect");
                if ("detail".equalsIgnoreCase(redirect)) {
                    response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&msg=assigned");
                } else {
                    response.sendRedirect(request.getContextPath() + "/tech-dashboard?msg=assigned");
                }
                return;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/tech-dashboard?error=invalid_id");
    }
}