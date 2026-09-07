package com.stackroute.helpdesk.dao;

import com.stackroute.helpdesk.model.TicketComment;
import com.stackroute.helpdesk.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public boolean addComment(TicketComment comment) {
        String sql = "INSERT INTO ticket_comments (ticket_id, author_id, comment_text) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, new String[] { "COMMENT_ID" });
            ps.setInt(1, comment.getTicketId());
            ps.setInt(2, comment.getAuthorId());
            ps.setString(3, comment.getCommentText());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    comment.setCommentId(rs.getInt(1));
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

    public List<TicketComment> getCommentsByTicketId(int ticketId) {
        List<TicketComment> list = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.ticket_id, c.author_id, c.comment_text, c.created_at, " +
                     "u.name AS author_name, u.role AS author_role " +
                     "FROM ticket_comments c " +
                     "JOIN users u ON c.author_id = u.user_id " +
                     "WHERE c.ticket_id = ? " +
                     "ORDER BY c.created_at ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ticketId);
            rs = ps.executeQuery();
            while (rs.next()) {
                TicketComment c = new TicketComment();
                c.setCommentId(rs.getInt("comment_id"));
                c.setTicketId(rs.getInt("ticket_id"));
                c.setAuthorId(rs.getInt("author_id"));
                c.setCommentText(rs.getString("comment_text"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setAuthorName(rs.getString("author_name"));
                c.setAuthorRole(rs.getString("author_role"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }
}