package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dao.UserDAO;
import com.stackroute.helpdesk.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if (user.isTechnician()) {
                response.sendRedirect(request.getContextPath() + "/tech-dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/tickets");
            }
            return;
        }
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        if (name == null || email == null || password == null || 
            name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "All fields are required.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        name = name.trim();
        email = email.trim().toLowerCase();
        password = password.trim();
        if (role == null || (!role.equalsIgnoreCase("TECHNICIAN") && !role.equalsIgnoreCase("USER"))) {
            role = "USER";
        } else {
            role = role.trim().toUpperCase();
        }

        if (userDAO.isEmailTaken(email)) {
            request.setAttribute("errorMessage", "Email is already registered. Please sign in or use another email.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);

        boolean success = userDAO.registerUser(newUser);
        if (success && newUser.getUserId() > 0) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", newUser);

            if (newUser.isTechnician()) {
                response.sendRedirect(request.getContextPath() + "/tech-dashboard?msg=registered");
            } else {
                response.sendRedirect(request.getContextPath() + "/tickets?msg=registered");
            }
        } else {
            request.setAttribute("errorMessage", "Registration failed due to a database error. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}