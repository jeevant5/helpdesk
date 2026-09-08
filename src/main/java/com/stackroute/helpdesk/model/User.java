package com.stackroute.helpdesk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Domain entity representing an authenticated Helpdesk user or technician.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String name;
    private String email;
    private String password;
    private String role; // USER, TECHNICIAN, ADMIN
    private String securityQuestion;
    private String securityAnswer;

    public User(int userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isTechnician() {
        return "TECHNICIAN".equalsIgnoreCase(this.role) || "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isEndUser() {
        return "USER".equalsIgnoreCase(this.role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}