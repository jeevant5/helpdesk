package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.model.Ticket;
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
import java.util.List;

@WebServlet("/tickets")
public class TicketListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService = new TicketServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String search = request.getParameter("search");
        String status = request.getParameter("status");
        String priority = request.getParameter("priority");

        TicketSearchCriteria criteria = TicketSearchCriteria.of(
            search, status, priority,
            currentUser.getUserId(),
            currentUser.isTechnician()
        );

        List<Ticket> tickets = ticketService.getTicketsByCriteria(criteria);

        request.setAttribute("tickets", tickets);
        request.setAttribute("searchKeyword", criteria.keyword() != null ? criteria.keyword() : "");
        request.setAttribute("selectedStatus", criteria.status());
        request.setAttribute("selectedPriority", criteria.priority());

        request.getRequestDispatcher("/user-tickets.jsp").forward(request, response);
    }
}