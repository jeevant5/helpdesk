package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/tech-dashboard")
public class TechnicianDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
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

        // 1. Real-time summary using aggregate queries (COUNT(*) GROUP BY status)
        Map<String, Integer> statusCounts = ticketDAO.getStatusCounts();
        int openCount = statusCounts.getOrDefault("OPEN", 0);
        int inProgressCount = statusCounts.getOrDefault("IN_PROGRESS", 0);
        int resolvedCount = statusCounts.getOrDefault("RESOLVED", 0);
        int closedCount = statusCounts.getOrDefault("CLOSED", 0);
        int totalTickets = openCount + inProgressCount + resolvedCount + closedCount;

        // 2. Unassigned tickets (available for self-assignment)
        List<Ticket> unassignedTickets = ticketDAO.getUnassignedTickets();

        // 3. Tickets currently claimed by this technician
        List<Ticket> myAssignedTickets = ticketDAO.getTicketsAssignedToTech(currentUser.getUserId());

        // 4. All system tickets
        List<Ticket> allTickets = ticketDAO.getAllTickets();

        request.setAttribute("openCount", openCount);
        request.setAttribute("inProgressCount", inProgressCount);
        request.setAttribute("resolvedCount", resolvedCount);
        request.setAttribute("closedCount", closedCount);
        request.setAttribute("totalTickets", totalTickets);
        request.setAttribute("unassignedTickets", unassignedTickets);
        request.setAttribute("myAssignedTickets", myAssignedTickets);
        request.setAttribute("allTickets", allTickets);

        request.getRequestDispatcher("/tech-dashboard.jsp").forward(request, response);
    }
}