package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.CommentDAO;
import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.TicketComment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/ticket-detail")
public class TicketDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TicketDAO ticketDAO = new TicketDAO();
    private CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }

        try {
            int ticketId = Integer.parseInt(idStr.trim());
            Ticket ticket = ticketDAO.getTicketById(ticketId);
            if (ticket == null) {
                response.sendRedirect(request.getContextPath() + "/tickets?error=not_found");
                return;
            }

            List<TicketComment> comments = commentDAO.getCommentsByTicketId(ticketId);

            request.setAttribute("ticket", ticket);
            request.setAttribute("comments", comments);
            request.getRequestDispatcher("/ticket-detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tickets");
        }
    }
}