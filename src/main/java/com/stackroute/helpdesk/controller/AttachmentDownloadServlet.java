package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.model.Ticket;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/attachment")
public class AttachmentDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing ticket id");
            return;
        }

        try {
            int ticketId = Integer.parseInt(idStr);
            Ticket ticket = ticketDAO.getTicketById(ticketId);
            if (ticket == null || !ticket.isHasAttachment()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Attachment not found");
                return;
            }

            String fileName = ticket.getAttachmentName() != null ? ticket.getAttachmentName() : "attachment.bin";
            String contentType = ticket.getAttachmentType() != null ? ticket.getAttachmentType() : "application/octet-stream";

            response.setContentType(contentType);
            // If image or text, display inline; else prompt download
            if (contentType.startsWith("image/") || contentType.startsWith("text/")) {
                response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }

            boolean written = ticketDAO.writeAttachment(ticketId, response.getOutputStream());
            if (!written) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}