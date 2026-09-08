package com.stackroute.helpdesk.service.impl;

import com.stackroute.helpdesk.dao.PasswordResetDAO;
import com.stackroute.helpdesk.dao.UserDAO;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.PasswordResetService;
import com.stackroute.helpdesk.util.EmailService;
import com.stackroute.helpdesk.util.ServiceResult;

import java.sql.Timestamp;
import java.util.UUID;

public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetDAO passwordResetDAO;
    private final UserDAO userDAO;

    public PasswordResetServiceImpl() {
        this.passwordResetDAO = new PasswordResetDAO();
        this.userDAO = new UserDAO();
    }

    public PasswordResetServiceImpl(PasswordResetDAO passwordResetDAO, UserDAO userDAO) {
        this.passwordResetDAO = passwordResetDAO;
        this.userDAO = userDAO;
    }

    @Override
    public ServiceResult<String> initiateReset(String email, String contextPath, String scheme, String serverName, int serverPort) {
        if (email == null || email.isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Please enter your registered email address.");
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            // For security, do not disclose whether email exists
            return ServiceResult.ok(null, "If an account exists with that email, a password reset link has been dispatched.");
        }

        String token = UUID.randomUUID().toString();
        // 30 minute expiration
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000L);

        boolean saved = passwordResetDAO.createResetToken(user.getUserId(), token, expiry);
        if (!saved) {
            return ServiceResult.fail("DB_ERROR", "Could not generate reset token. Please try again.");
        }

        // Build reset URL
        String portPart = ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
            ? "" : ":" + serverPort;
        String resetUrl = scheme + "://" + serverName + portPart + contextPath + "/reset-password?token=" + token;

        // Dispatch email via Jakarta Mail
        EmailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetUrl);

        return ServiceResult.ok(resetUrl, "Password reset instructions have been sent to your email.");
    }

    @Override
    public ServiceResult<Integer> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return ServiceResult.fail("INVALID_TOKEN", "Reset token is missing.");
        }

        Integer userId = passwordResetDAO.getUserIdByValidToken(token.trim());
        if (userId == null) {
            return ServiceResult.fail("EXPIRED_TOKEN", "Password reset link is invalid or has expired.");
        }

        return ServiceResult.ok(userId, "Token is valid.");
    }

    @Override
    public ServiceResult<Void> completeReset(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            return ServiceResult.fail("INVALID_TOKEN", "Reset token is missing.");
        }
        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Please fill in all password fields.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail("PASSWORD_MISMATCH", "New passwords do not match.");
        }
        if (newPassword.length() < 6) {
            return ServiceResult.fail("WEAK_PASSWORD", "Password must be at least 6 characters long.");
        }

        Integer userId = passwordResetDAO.getUserIdByValidToken(token.trim());
        if (userId == null) {
            return ServiceResult.fail("EXPIRED_TOKEN", "Password reset link is invalid or has expired.");
        }

        boolean updated = passwordResetDAO.updatePassword(userId, newPassword);
        if (updated) {
            passwordResetDAO.markTokenUsed(token.trim());
            return ServiceResult.ok(null, "Your password has been successfully reset. Please log in with your new password.");
        }

        return ServiceResult.fail("DB_ERROR", "Could not update password in database. Please try again.");
    }

    @Override
    public ServiceResult<String> getSecurityQuestion(String email) {
        if (email == null || email.isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Please enter your registered email address.");
        }
        String question = userDAO.getSecurityQuestionByEmail(email.trim().toLowerCase());
        if (question == null) {
            return ServiceResult.fail("NOT_FOUND", "No registered account found with that email address.");
        }
        return ServiceResult.ok(question, "Security question retrieved successfully.");
    }

    @Override
    public ServiceResult<Void> resetPasswordWithSecurityQuestion(com.stackroute.helpdesk.dto.SecurityQuestionResetDTO dto) {
        if (dto == null || dto.hasEmptyFields()) {
            return ServiceResult.fail("VALIDATION_ERROR", "All fields are required to reset your password.");
        }
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            return ServiceResult.fail("PASSWORD_MISMATCH", "New passwords do not match.");
        }
        if (dto.newPassword().trim().length() < 4) {
            return ServiceResult.fail("WEAK_PASSWORD", "Password must be at least 4 characters long.");
        }

        boolean updated = userDAO.verifySecurityAnswerAndUpdatePassword(
            dto.email().trim().toLowerCase(),
            dto.securityAnswer().trim(),
            dto.newPassword().trim()
        );

        if (updated) {
            return ServiceResult.ok(null, "Password reset successfully! Please sign in with your new password.");
        }

        return ServiceResult.fail("SECURITY_ANSWER_MISMATCH", "Incorrect security answer. Please check and try again.");
    }
}