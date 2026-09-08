package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.service.PasswordResetService;
import com.stackroute.helpdesk.service.impl.PasswordResetServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PasswordResetService passwordResetService = new PasswordResetServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String token = request.getParameter("token");
        ServiceResult<Integer> validation = passwordResetService.validateToken(token);

        if (validation.isSuccess()) {
            request.setAttribute("token", token != null ? token.trim() : "");
            request.setAttribute("validToken", true);
        } else {
            request.setAttribute("errorMessage", validation.getMessage());
        }

        request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String newPassword = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        ServiceResult<Void> result = passwordResetService.completeReset(token, newPassword, confirmPassword);
        if (result.isSuccess()) {
            response.sendRedirect(request.getContextPath() + "/login?msg=password_reset_success");
        } else {
            request.setAttribute("errorMessage", result.getMessage());
            request.setAttribute("token", token);
            request.setAttribute("validToken", true);
            request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
        }
    }
}