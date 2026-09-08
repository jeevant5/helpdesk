package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.UserService;
import com.stackroute.helpdesk.service.impl.UserServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            response.sendRedirect(request.getContextPath() + (user.isTechnician() ? "/tech-dashboard" : "/tickets"));
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        ServiceResult<User> result = userService.authenticate(email, password);
        if (result instanceof ServiceResult.Success<User> success) {
            User user = success.data();
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + (user.isTechnician() ? "/tech-dashboard" : "/tickets"));
        } else {
            request.setAttribute("errorMessage", result.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}