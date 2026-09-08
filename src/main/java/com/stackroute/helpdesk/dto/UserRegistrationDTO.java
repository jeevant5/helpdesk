package com.stackroute.helpdesk.dto;

/**
 * Java 17 Record capturing incoming user registration form submissions.
 */
public record UserRegistrationDTO(
    String name,
    String email,
    String password,
    String role,
    String captchaInput,
    String sessionCaptcha,
    String securityQuestion,
    String securityAnswer
) {
    public UserRegistrationDTO {
        role = (role != null && !role.isBlank()) ? role.trim().toUpperCase() : "USER";
    }

    public UserRegistrationDTO(String name, String email, String password, String role, String captchaInput, String sessionCaptcha) {
        this(name, email, password, role, captchaInput, sessionCaptcha, "What was the name of your first pet?", "fluffy");
    }

    public boolean isCaptchaValid() {
        if (sessionCaptcha == null || sessionCaptcha.isBlank()) {
            return true; // No captcha challenge in session
        }
        return captchaInput != null && captchaInput.trim().equalsIgnoreCase(sessionCaptcha.trim());
    }

    public boolean hasEmptyFields() {
        return name == null || name.isBlank() ||
               email == null || email.isBlank() ||
               password == null || password.isBlank() ||
               securityQuestion == null || securityQuestion.isBlank() ||
               securityAnswer == null || securityAnswer.isBlank();
    }
}