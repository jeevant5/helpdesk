package com.stackroute.helpdesk.dto;

/**
 * Java 17 Record capturing security question verification and password reset submissions.
 */
public record SecurityQuestionResetDTO(
    String email,
    String securityAnswer,
    String newPassword,
    String confirmPassword
) {
    public boolean hasEmptyFields() {
        return email == null || email.isBlank() ||
               securityAnswer == null || securityAnswer.isBlank() ||
               newPassword == null || newPassword.isBlank() ||
               confirmPassword == null || confirmPassword.isBlank();
    }
}
