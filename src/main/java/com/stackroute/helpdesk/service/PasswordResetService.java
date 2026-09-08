package com.stackroute.helpdesk.service;

import com.stackroute.helpdesk.util.ServiceResult;

public interface PasswordResetService {
    ServiceResult<String> initiateReset(String email, String contextPath, String scheme, String serverName, int serverPort);
    ServiceResult<Integer> validateToken(String token);
    ServiceResult<Void> completeReset(String token, String newPassword, String confirmPassword);
    ServiceResult<String> getSecurityQuestion(String email);
    ServiceResult<Void> resetPasswordWithSecurityQuestion(com.stackroute.helpdesk.dto.SecurityQuestionResetDTO dto);
}