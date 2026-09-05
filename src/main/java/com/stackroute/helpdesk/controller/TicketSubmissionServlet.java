package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@WebServlet("/submit-ticket")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB memory buffer
    maxFileSize = 10 * 1024 * 1024,       // 10MB max upload size
    maxRequestSize = 25 * 1024 * 1024     // 25MB max request size
)
public class TicketSubmissionServlet extends HttpServlet {
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
        request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String priority = request.getParameter("priority");

        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Title and Description are required.");
            request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
            return;
        }

        Ticket ticket = new Ticket();
        ticket.setUserId(currentUser.getUserId());
        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setPriority(priority != null ? priority.trim().toUpperCase() : "MEDIUM");
        ticket.setStatus("OPEN");

        // Handle attachment file part
        Part filePart = request.getPart("attachment");
        InputStream fileContent = null;
        long fileSize = 0;
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            ticket.setAttachmentName(fileName);
            ticket.setAttachmentType(filePart.getContentType());
            fileContent = filePart.getInputStream();
            fileSize = filePart.getSize();
        }

        try {
            int ticketId = ticketDAO.createTicket(ticket, fileContent, fileSize);
            if (ticketId > 0) {
                response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + ticketId + "&msg=created");
            } else {
                request.setAttribute("errorMessage", "Failed to submit ticket. Please try again.");
                request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
        }
    }
}