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

@WebServlet("/tickets")
public class TicketListServlet extends HttpServlet {
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
        List<Ticket> tickets;
        if (currentUser.isTechnician()) {
            tickets = ticketDAO.getAllTickets();
        } else {
            tickets = ticketDAO.getTicketsByUserId(currentUser.getUserId());
        }

        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher("/user-tickets.jsp").forward(request, response);
    }
}