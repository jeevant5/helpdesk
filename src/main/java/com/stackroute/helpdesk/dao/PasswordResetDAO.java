package com.stackroute.helpdesk.dao;

import com.stackroute.helpdesk.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PasswordResetDAO {

    public boolean createResetToken(int userId, String token, Timestamp expiry) {
        String sql = "INSERT INTO password_resets (user_id, reset_token, expiry_time, used) VALUES (?, ?, ?, 0)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, new String[] { "RESET_ID" });
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, expiry);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    public Integer getUserIdByValidToken(String token) {
        String sql = "SELECT user_id, expiry_time, used FROM password_resets WHERE reset_token = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, token);
            rs = ps.executeQuery();
            if (rs.next()) {
                int used = rs.getInt("used");
                Timestamp expiry = rs.getTimestamp("expiry_time");
                long now = System.currentTimeMillis();
                if (used == 0 && expiry != null && expiry.getTime() > now) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public boolean markTokenUsed(String token) {
        String sql = "UPDATE password_resets SET used = 1 WHERE reset_token = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    public int cleanupExpiredTokens() {
        String sql = "DELETE FROM password_resets WHERE used = 1 OR expiry_time < SYSDATE";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }
}