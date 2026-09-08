package com.stackroute.helpdesk.service;

import com.stackroute.helpdesk.dto.UserRegistrationDTO;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.util.ServiceResult;

import java.util.Optional;

public interface UserService {
    ServiceResult<User> authenticate(String email, String password);
    ServiceResult<User> register(UserRegistrationDTO dto);
    Optional<User> findByEmail(String email);
    boolean isEmailTaken(String email);
}