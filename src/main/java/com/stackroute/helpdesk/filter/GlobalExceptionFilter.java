package com.stackroute.helpdesk.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Exception Filter that intercepts unhandled runtime exceptions,
 * assigns a unique Incident Tracking ID, logs diagnostic details,
 * and renders a polished, user-friendly error page.
 */
@WebFilter(filterName = "GlobalExceptionFilter", urlPatterns = "/*")
public class GlobalExceptionFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            handleException(request, response, t);
        }
    }

    private void handleException(ServletRequest request, ServletResponse response, Throwable t)
            throws IOException, ServletException {
        String incidentId = "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        LOGGER.log(Level.SEVERE, String.format(
            "[%s] Unhandled Exception on URI: %s | Client IP: %s",
            incidentId, req.getRequestURI(), req.getRemoteAddr()
        ), t);

        if (!resp.isCommitted()) {
            req.setAttribute("incidentId", incidentId);
            req.setAttribute("incidentTimestamp", timestamp);
            req.setAttribute("userFriendlyMessage", "Error processing request. Please try again later.");
            req.setAttribute("exceptionMessage", t.getMessage());
            
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}