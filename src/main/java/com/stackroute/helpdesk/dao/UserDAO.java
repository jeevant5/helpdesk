package com.stackroute.helpdesk.dao;

import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String email, String password) {
        String sql = "SELECT user_id, name, email, password, role, security_question, security_answer FROM users WHERE LOWER(email) = LOWER(?) AND password = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setSecurityQuestion(rs.getString("security_question"));
                u.setSecurityAnswer(rs.getString("security_answer"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT user_id, name, email, password, role, security_question, security_answer FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setSecurityQuestion(rs.getString("security_question"));
                u.setSecurityAnswer(rs.getString("security_answer"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public boolean isEmailTaken(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return false;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, security_question, security_answer) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, new String[] { "USER_ID" });
            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole() != null ? user.getRole().toUpperCase() : "USER");
            ps.setString(5, user.getSecurityQuestion() != null ? user.getSecurityQuestion().trim() : "What was the name of your first pet?");
            ps.setString(6, user.getSecurityAnswer() != null ? user.getSecurityAnswer().trim().toLowerCase() : "fluffy");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return false;
    }

    public User findById(int userId) {
        String sql = "SELECT user_id, name, email, password, role, security_question, security_answer FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setSecurityQuestion(rs.getString("security_question"));
                u.setSecurityAnswer(rs.getString("security_answer"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public String getSecurityQuestionByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        String sql = "SELECT security_question FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                String question = rs.getString("security_question");
                return (question != null && !question.isBlank()) ? question : "What was the name of your first pet?";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public boolean verifySecurityAnswerAndUpdatePassword(String email, String answer, String newPassword) {
        if (email == null || answer == null || newPassword == null) return false;
        String sql = "SELECT user_id, security_answer FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String storedAnswer = rs.getString("security_answer");
                if (storedAnswer != null && storedAnswer.trim().equalsIgnoreCase(answer.trim())) {
                    // Answer matches! Update password
                    try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")) {
                        updatePs.setString(1, newPassword.trim());
                        updatePs.setInt(2, userId);
                        return updatePs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return false;
    }

    public List<User> getAllTechnicians() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, name, email, role FROM users WHERE role IN ('TECHNICIAN', 'ADMIN') ORDER BY name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }
}