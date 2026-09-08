package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
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
    fileSizeThreshold = 1024 * 1024,      // 1MB buffer
    maxFileSize = 10 * 1024 * 1024,       // 10MB max upload
    maxRequestSize = 25 * 1024 * 1024     // 25MB request limit
)
public class TicketSubmissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService = new TicketServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String priority = request.getParameter("priority");

        Ticket ticket = Ticket.builder()
            .userId(currentUser.getUserId())
            .title(title)
            .description(description)
            .priority(priority)
            .build();

        InputStream attachmentStream = null;
        long attachmentSize = 0;

        Part filePart = request.getPart("attachment");
        if (filePart != null && filePart.getSize() > 0) {
            String submittedFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            ticket.setAttachmentName(submittedFileName);
            ticket.setAttachmentType(filePart.getContentType());
            attachmentStream = filePart.getInputStream();
            attachmentSize = filePart.getSize();
        }

        ServiceResult<Ticket> result = ticketService.createTicket(ticket, attachmentStream, attachmentSize);
        if (result instanceof ServiceResult.Success<Ticket> success) {
            response.sendRedirect(request.getContextPath() + "/ticket-detail?id=" + success.data().getTicketId() + "&msg=created");
        } else {
            request.setAttribute("errorMessage", result.getMessage());
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.setAttribute("priority", priority);
            request.getRequestDispatcher("/submit-ticket.jsp").forward(request, response);
        }
    }
}