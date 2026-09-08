package com.stackroute.helpdesk.dao;

import com.stackroute.helpdesk.model.TicketFeedback;
import com.stackroute.helpdesk.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackDAO {

    public boolean addFeedback(TicketFeedback feedback) {
        String sql = "INSERT INTO ticket_feedback (ticket_id, rating, notes) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, new String[] { "FEEDBACK_ID" });
            ps.setInt(1, feedback.getTicketId());
            ps.setInt(2, feedback.getRating());
            ps.setString(3, feedback.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    public TicketFeedback getFeedbackByTicketId(int ticketId) {
        String sql = "SELECT feedback_id, ticket_id, rating, notes, created_at FROM ticket_feedback WHERE ticket_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ticketId);
            rs = ps.executeQuery();
            if (rs.next()) {
                TicketFeedback f = new TicketFeedback();
                f.setFeedbackId(rs.getInt("feedback_id"));
                f.setTicketId(rs.getInt("ticket_id"));
                f.setRating(rs.getInt("rating"));
                f.setNotes(rs.getString("notes"));
                f.setCreatedAt(rs.getTimestamp("created_at"));
                return f;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public boolean submitFeedback(TicketFeedback feedback) {
        return addFeedback(feedback);
    }

    public boolean hasFeedback(int ticketId) {
        return getFeedbackByTicketId(ticketId) != null;
    }

    public double getAverageRating() {
        String sql = "SELECT NVL(AVG(rating), 0.0) AS avg_rating FROM ticket_feedback";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0.0;
    }
}