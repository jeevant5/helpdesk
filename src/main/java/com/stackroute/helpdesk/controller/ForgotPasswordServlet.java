package com.stackroute.helpdesk.controller;

import com.stackroute.helpdesk.service.PasswordResetService;
import com.stackroute.helpdesk.service.impl.PasswordResetServiceImpl;
import com.stackroute.helpdesk.util.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PasswordResetService passwordResetService = new PasswordResetServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getSession(true);
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String email = request.getParameter("email");

        // STEP 2: Verify Security Answer and Reset Password
        if ("reset_with_question".equals(action)) {
            String securityAnswer = request.getParameter("securityAnswer");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");
            String securityQuestion = request.getParameter("securityQuestion");

            com.stackroute.helpdesk.dto.SecurityQuestionResetDTO resetDTO = 
                new com.stackroute.helpdesk.dto.SecurityQuestionResetDTO(email, securityAnswer, newPassword, confirmPassword);

            ServiceResult<Void> resetResult = passwordResetService.resetPasswordWithSecurityQuestion(resetDTO);
            if (resetResult.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/login?msg=password_reset");
                return;
            } else {
                request.setAttribute("errorMessage", resetResult.getMessage());
                request.setAttribute("email", email);
                request.setAttribute("securityQuestion", securityQuestion);
                request.setAttribute("step", 2);
                request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
                return;
            }
        }

        // STEP 1: Verify CAPTCHA and Retrieve User's Registered Security Question
        String captchaInput = request.getParameter("captcha");
        HttpSession session = request.getSession(false);
        String sessionCaptcha = session != null ? (String) session.getAttribute("CAPTCHA_CODE") : null;

        if (captchaInput == null || sessionCaptcha == null || !captchaInput.trim().equalsIgnoreCase(sessionCaptcha)) {
            request.setAttribute("errorMessage", "Invalid CAPTCHA security code. Please try again.");
            request.setAttribute("email", email);
            request.setAttribute("step", 1);
            request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            return;
        }

        if (session != null) {
            session.removeAttribute("CAPTCHA_CODE");
        }

        ServiceResult<String> questionResult = passwordResetService.getSecurityQuestion(email);
        if (questionResult.isFailure()) {
            request.setAttribute("errorMessage", questionResult.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("step", 1);
            request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            return;
        }

        // Account found: transition to Step 2 with the retrieved Security Question
        String question = questionResult.getData().orElse("What was the name of your first pet?");
        request.setAttribute("step", 2);
        request.setAttribute("email", email != null ? email.trim().toLowerCase() : "");
        request.setAttribute("securityQuestion", question);

        // Also initiate background token for on-screen reset link fallback
        ServiceResult<String> tokenResult = passwordResetService.initiateReset(
            email,
            request.getContextPath(),
            request.getScheme(),
            request.getServerName(),
            request.getServerPort()
        );
        tokenResult.getData().ifPresent(url -> {
            request.setAttribute("resetUrl", url);
            request.setAttribute("devResetUrl", url);
        });

        boolean smtpActive = com.stackroute.helpdesk.util.EmailService.isSmtpConfigured();
        request.setAttribute("isSmtpConfigured", smtpActive);

        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }
}