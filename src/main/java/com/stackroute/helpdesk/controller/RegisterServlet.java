package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.dto.UserRegistrationDTO;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getSession(true);
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String sessionCaptcha = (session != null) ? (String) session.getAttribute("CAPTCHA_CODE") : null;

        UserRegistrationDTO dto = new UserRegistrationDTO(
            request.getParameter("name"),
            request.getParameter("email"),
            request.getParameter("password"),
            "USER",
            request.getParameter("captcha"),
            sessionCaptcha,
            request.getParameter("securityQuestion"),
            request.getParameter("securityAnswer")
        );

        ServiceResult<User> result = userService.register(dto);
        if (result instanceof ServiceResult.Success<User> success) {
            User user = success.data();
            // Invalidate any existing session and establish fresh authenticated session
            if (session != null) {
                session.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("user", user);

            response.sendRedirect(request.getContextPath() + "/tickets?msg=registered");
        } else {
            if (session != null) {
                session.removeAttribute("CAPTCHA_CODE");
            }
            request.setAttribute("errorMessage", result.getMessage());
            request.setAttribute("name", dto.name());
            request.setAttribute("email", dto.email());
            request.setAttribute("selectedQuestion", dto.securityQuestion());
            request.setAttribute("securityAnswer", dto.securityAnswer());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}