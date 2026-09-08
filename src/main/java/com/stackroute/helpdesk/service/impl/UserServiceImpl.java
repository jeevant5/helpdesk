package com.stackroute.helpdesk.service.impl;

import com.stackroute.helpdesk.dao.UserDAO;
import com.stackroute.helpdesk.dto.UserRegistrationDTO;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.UserService;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAO();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public ServiceResult<User> authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ServiceResult.fail("EMPTY_CREDENTIALS", "Email and password are required.");
        }

        User user = userDAO.authenticate(email.trim().toLowerCase(), password.trim());
        if (user == null) {
            return ServiceResult.fail("INVALID_CREDENTIALS", "Invalid email address or password.");
        }

        return ServiceResult.ok(user, "User authenticated successfully.");
    }

    @Override
    public ServiceResult<User> register(UserRegistrationDTO dto) {
        if (dto == null || dto.hasEmptyFields()) {
            return ServiceResult.fail("VALIDATION_ERROR", "All required fields must be completed.");
        }

        if (!dto.isCaptchaValid()) {
            return ServiceResult.fail("CAPTCHA_MISMATCH", "Invalid security verification (CAPTCHA) code. Please try again.");
        }

        String email = dto.email().trim().toLowerCase();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ServiceResult.fail("INVALID_EMAIL", "Please enter a valid email address.");
        }

        if (dto.password().trim().length() < 4) {
            return ServiceResult.fail("WEAK_PASSWORD", "Password must contain at least 4 characters.");
        }

        if (userDAO.isEmailTaken(email)) {
            return ServiceResult.fail("EMAIL_EXISTS", "Email is already registered. Please sign in or use another email.");
        }

        String role = Optional.ofNullable(dto.role())
            .map(String::trim)
            .map(String::toUpperCase)
            .filter(r -> "TECHNICIAN".equals(r) || "USER".equals(r))
            .orElse("USER");

        User newUser = User.builder()
            .name(dto.name().trim())
            .email(email)
            .password(dto.password().trim())
            .role(role)
            .securityQuestion(dto.securityQuestion() != null ? dto.securityQuestion().trim() : "What was the name of your first pet?")
            .securityAnswer(dto.securityAnswer() != null ? dto.securityAnswer().trim() : "fluffy")
            .build();

        boolean registered = userDAO.registerUser(newUser);
        if (registered && newUser.getUserId() > 0) {
            return ServiceResult.ok(newUser, "Account created successfully.");
        }

        return ServiceResult.fail("DB_ERROR", "Registration failed due to a database error. Please try again.");
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(userDAO.findByEmail(email.trim().toLowerCase()));
    }

    @Override
    public boolean isEmailTaken(String email) {
        if (email == null || email.isBlank()) return false;
        return userDAO.isEmailTaken(email.trim().toLowerCase());
    }
}