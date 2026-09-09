package com.stackroute.helpdesk.controller;

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
import java.io.OutputStream;
import java.util.Optional;

@WebServlet(urlPatterns = {"/attachment", "/download-attachment"})
public class AttachmentDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService;

    public AttachmentDownloadServlet() {
        this(new TicketServiceImpl());
    }

    public AttachmentDownloadServlet(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String ticketIdStr = request.getParameter("id");
        if (ticketIdStr == null || ticketIdStr.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ticket ID is required.");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());
            Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);

            if (ticketOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket not found for #" + ticketId);
                return;
            }

            Ticket ticket = ticketOpt.get();

            HttpSession session = request.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

            if (currentUser == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please sign in to download attachments.");
                return;
            }

            // Strict privacy: Regular users cannot download attachments of other users' tickets
            if (!currentUser.isTechnician() && ticket.getUserId() != currentUser.getUserId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You cannot download attachments for other users' tickets.");
                return;
            }

            if (!ticket.isHasAttachment()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Attachment not found for ticket #" + ticketId);
                return;
            }

            String contentType = ticket.getAttachmentType() != null ? ticket.getAttachmentType() : "application/octet-stream";
            String rawFileName = ticket.getAttachmentName() != null ? ticket.getAttachmentName() : "attachment";
            String cleanFileName = rawFileName.replace("\r", "").replace("\n", "").replace("\"", "");

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + cleanFileName + "\"");

            try (OutputStream out = response.getOutputStream()) {
                boolean success = ticketService.writeAttachment(ticketId, out);
                if (!success && !response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not stream BLOB attachment.");
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ticket ID format.");
        }
    }
}