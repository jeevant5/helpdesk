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
    private final TicketService ticketService = new TicketServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String ticketIdStr = request.getParameter("ticketId");
        if (ticketIdStr == null || ticketIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/tech-dashboard?error=invalid_id");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());
            ServiceResult<Void> result = ticketService.assignTicket(ticketId, currentUser.getUserId());

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