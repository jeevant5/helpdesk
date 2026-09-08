package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/export-tickets")
public class ExportTicketsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService = new TicketServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthenticated");
            return;
        }

        // Restrict export capability strictly to technicians / administrators
        if (!currentUser.isTechnician()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Ticket export is restricted to technicians.");
            return;
        }

        String keyword = request.getParameter("search");
        String status = request.getParameter("status");
        String priority = request.getParameter("priority");

        TicketSearchCriteria criteria = TicketSearchCriteria.of(
            keyword, status, priority,
            currentUser.getUserId(),
            currentUser.isTechnician()
        );

        String csvContent = ticketService.generateCsvReport(criteria);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"helpdesk_tickets_export.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(csvContent);
        }
    }
}