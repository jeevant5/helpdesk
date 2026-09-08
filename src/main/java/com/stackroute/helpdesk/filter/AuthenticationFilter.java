package com.stackroute.helpdesk.filter;

import com.stackroute.helpdesk.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Role-Based Access Control (RBAC) and Session Security Filter.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/", "/index.jsp", "/login", "/login.jsp",
        "/register", "/register.jsp", "/forgot-password", "/forgot-password.jsp",
        "/reset-password", "/reset-password.jsp", "/captcha", "/logout", "/error.jsp"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath() != null ? req.getServletPath() : "";

        // Allow static assets (css, js, images, bootstrap, etc.)
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
            path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") ||
            path.endsWith(".jpg") || path.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public paths
        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Authentication guard
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=unauthenticated");
            return;
        }

        // Technicians are not allowed to submit new tickets (they resolve them)
        if ("/submit-ticket".equals(path) && user.isTechnician()) {
            resp.sendRedirect(req.getContextPath() + "/tech-dashboard?error=unauthorized_create");
            return;
        }

        // Non-technicians cannot access technician command center
        if ("/tech-dashboard".equals(path) && !user.isTechnician()) {
            resp.sendRedirect(req.getContextPath() + "/tickets?error=unauthorized");
            return;
        }

        // Non-technicians cannot export tickets
        if ("/export-tickets".equals(path) && !user.isTechnician()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Ticket export is restricted to technicians.");
            return;
        }

        chain.doFilter(request, response);
    }
}