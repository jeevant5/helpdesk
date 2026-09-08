package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dto.TicketStatisticsDTO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.UserService;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import com.stackroute.helpdesk.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/tech-dashboard")
public class TechnicianDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService;
    private final UserService userService;

    public TechnicianDashboardServlet() {
        this(new TicketServiceImpl(), new UserServiceImpl());
    }

    public TechnicianDashboardServlet(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // Compute aggregate metrics using Java Streams in Service layer
        TicketStatisticsDTO stats = ticketService.getTicketStatistics();

        // Queues sorted by urgency
        List<Ticket> unassignedTickets = ticketService.getUnassignedTickets();
        List<User> technicians = userService.getAvailableTechnicians();

        List<Ticket> assignedTickets;
        if (currentUser != null && currentUser.isAdmin()) {
            // Admin sees all assigned tickets across all technicians
            assignedTickets = ticketService.getAllAssignedTickets();
        } else if (currentUser != null) {
            // Technician sees only their own assigned tickets
            assignedTickets = ticketService.getTicketsAssignedToTech(currentUser.getUserId());
        } else {
            assignedTickets = List.of();
        }

        request.setAttribute("openCount", stats.openCount());
        request.setAttribute("inProgressCount", stats.inProgressCount());
        request.setAttribute("resolvedCount", stats.resolvedCount());
        request.setAttribute("closedCount", stats.closedCount());
        request.setAttribute("totalTickets", stats.totalCount());
        request.setAttribute("breachedCount", stats.breachedCount());
        request.setAttribute("unassignedTickets", unassignedTickets);
        request.setAttribute("assignedTickets", assignedTickets);
        request.setAttribute("myAssignedTickets", assignedTickets);
        request.setAttribute("technicians", technicians);

        request.getRequestDispatcher("/tech-dashboard.jsp").forward(request, response);
    }
}