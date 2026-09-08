package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.service.impl.TicketServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/ticket-delete")
public class TicketDeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TicketService ticketService;

    public TicketDeleteServlet() {
        this.ticketService = new TicketServiceImpl();
    }

    public TicketDeleteServlet(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthenticated");
            return;
        }

        String action = request.getParameter("action");

        if ("purgeAll".equalsIgnoreCase(action)) {
            if (!currentUser.isTechnician() && !currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only technicians or admins can purge tickets.");
                return;
            }

            ServiceResult<Integer> purgeResult = ticketService.deleteAllTickets(currentUser);
            if (purgeResult.isSuccess()) {
                int count = purgeResult.getData().orElse(0);
                response.sendRedirect(request.getContextPath() + "/tech-dashboard?msg=all_purged&count=" + count);
            } else {
                response.sendRedirect(request.getContextPath() + "/tech-dashboard?error=purge_failed");
            }
            return;
        }

        // Single ticket deletion
        String ticketIdStr = request.getParameter("ticketId");
        if (ticketIdStr == null || ticketIdStr.isBlank()) {
            redirectAfterDelete(request, response, currentUser, false, "invalid_id");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr.trim());
            ServiceResult<Void> deleteResult = ticketService.deleteTicket(ticketId, currentUser);

            if (deleteResult.isSuccess()) {
                redirectAfterDelete(request, response, currentUser, true, "ticket_deleted");
            } else {
                if ("FORBIDDEN".equals(deleteResult.getErrorCode())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, deleteResult.getErrorMessage());
                } else {
                    redirectAfterDelete(request, response, currentUser, false, "delete_failed");
                }
            }
        } catch (NumberFormatException e) {
            redirectAfterDelete(request, response, currentUser, false, "invalid_id");
        }
    }

    private void redirectAfterDelete(HttpServletRequest request, HttpServletResponse response, 
                                     User user, boolean success, String msgCode) throws IOException {
        String param = success ? "msg=" + msgCode : "error=" + msgCode;
        if (user != null && (user.isTechnician() || user.isAdmin())) {
            response.sendRedirect(request.getContextPath() + "/tech-dashboard?" + param);
        } else {
            response.sendRedirect(request.getContextPath() + "/tickets?" + param);
        }
    }
}
