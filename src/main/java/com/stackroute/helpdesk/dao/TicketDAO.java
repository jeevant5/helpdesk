package com.stackroute.helpdesk.dao;

import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.util.DBUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;

public class TicketDAO {

    public int createTicket(Ticket ticket, InputStream attachmentStream, long attachmentSize) throws SQLException {
        String sql = "INSERT INTO tickets (user_id, title, description, priority, status, attachment, attachment_name, attachment_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, new String[] { "TICKET_ID" });
            ps.setInt(1, ticket.getUserId());
            ps.setString(2, ticket.getTitle());
            ps.setString(3, ticket.getDescription());
            ps.setString(4, ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM");
            ps.setString(5, ticket.getStatus() != null ? ticket.getStatus() : "OPEN");

            if (attachmentStream != null && attachmentSize > 0) {
                ps.setBinaryStream(6, attachmentStream, attachmentSize);
                ps.setString(7, ticket.getAttachmentName());
                ps.setString(8, ticket.getAttachmentType());
            } else {
                ps.setNull(6, Types.BLOB);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                rs = ps.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    int generatedId = rs.getInt(1);
                    ticket.setTicketId(generatedId);
                    return generatedId;
                }
            }
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public Ticket getTicketById(int ticketId) {
        String sql = "SELECT t.ticket_id, t.user_id, t.tech_id, t.title, t.description, t.priority, t.status, " +
                     "t.attachment_name, t.attachment_type, " +
                     "CASE WHEN t.attachment IS NOT NULL THEN 1 ELSE 0 END AS has_attachment, t.created_at, " +
                     "u.name AS user_name, u.email AS user_email, tech.name AS tech_name " +
                     "FROM tickets t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN users tech ON t.tech_id = tech.user_id " +
                     "WHERE t.ticket_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ticketId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public List<Ticket> getTicketsByUserId(int userId) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.user_id, t.tech_id, t.title, t.description, t.priority, t.status, " +
                     "t.attachment_name, t.attachment_type, " +
                     "CASE WHEN t.attachment IS NOT NULL THEN 1 ELSE 0 END AS has_attachment, t.created_at, " +
                     "u.name AS user_name, u.email AS user_email, tech.name AS tech_name " +
                     "FROM tickets t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN users tech ON t.tech_id = tech.user_id " +
                     "WHERE t.user_id = ? ORDER BY t.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.user_id, t.tech_id, t.title, t.description, t.priority, t.status, " +
                     "t.attachment_name, t.attachment_type, " +
                     "CASE WHEN t.attachment IS NOT NULL THEN 1 ELSE 0 END AS has_attachment, t.created_at, " +
                     "u.name AS user_name, u.email AS user_email, tech.name AS tech_name " +
                     "FROM tickets t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN users tech ON t.tech_id = tech.user_id " +
                     "ORDER BY t.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public List<Ticket> getUnassignedTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.user_id, t.tech_id, t.title, t.description, t.priority, t.status, " +
                     "t.attachment_name, t.attachment_type, " +
                     "CASE WHEN t.attachment IS NOT NULL THEN 1 ELSE 0 END AS has_attachment, t.created_at, " +
                     "u.name AS user_name, u.email AS user_email, tech.name AS tech_name " +
                     "FROM tickets t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN users tech ON t.tech_id = tech.user_id " +
                     "WHERE t.tech_id IS NULL AND t.status != 'RESOLVED' AND t.status != 'CLOSED' " +
                     "ORDER BY CASE t.priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, t.created_at ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public List<Ticket> getTicketsAssignedToTech(int techId) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.user_id, t.tech_id, t.title, t.description, t.priority, t.status, " +
                     "t.attachment_name, t.attachment_type, " +
                     "CASE WHEN t.attachment IS NOT NULL THEN 1 ELSE 0 END AS has_attachment, t.created_at, " +
                     "u.name AS user_name, u.email AS user_email, tech.name AS tech_name " +
                     "FROM tickets t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN users tech ON t.tech_id = tech.user_id " +
                     "WHERE t.tech_id = ? " +
                     "ORDER BY CASE t.status WHEN 'IN_PROGRESS' THEN 1 WHEN 'OPEN' THEN 2 ELSE 3 END, t.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, techId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    // Required by specification: Real-time summary using aggregate queries (COUNT(*) GROUP BY status)
    public Map<String, Integer> getStatusCounts() {
        Map<String, Integer> map = new HashMap<>();
        map.put("OPEN", 0);
        map.put("IN_PROGRESS", 0);
        map.put("RESOLVED", 0);
        map.put("CLOSED", 0);

        String sql = "SELECT status, COUNT(*) AS count_val FROM tickets GROUP BY status";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count_val");
                if (status != null) {
                    map.put(status.toUpperCase(), count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return map;
    }

    // Required by specification: Technicians assign tickets to themselves
    public boolean assignTicket(int ticketId, int techId) {
        String sql = "UPDATE tickets SET tech_id = ?, status = CASE WHEN status = 'OPEN' THEN 'IN_PROGRESS' ELSE status END WHERE ticket_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, techId);
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    // Required by specification: dynamically changing ticket status from OPEN to IN_PROGRESS or RESOLVED
    public boolean updateStatus(int ticketId, String newStatus) {
        String sql = "UPDATE tickets SET status = ? WHERE ticket_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus.toUpperCase());
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    // Streams BLOB directly out for downloading/displaying
    public boolean writeAttachment(int ticketId, OutputStream out) {
        String sql = "SELECT attachment FROM tickets WHERE ticket_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ticketId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Blob blob = rs.getBlob("attachment");
                if (blob != null) {
                    try (InputStream in = blob.getBinaryStream()) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        out.flush();
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return false;
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setTicketId(rs.getInt("ticket_id"));
        t.setUserId(rs.getInt("user_id"));
        int techId = rs.getInt("tech_id");
        if (!rs.wasNull()) {
            t.setTechId(techId);
        }
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setAttachmentName(rs.getString("attachment_name"));
        t.setAttachmentType(rs.getString("attachment_type"));
        t.setHasAttachment(rs.getInt("has_attachment") == 1);
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUserName(rs.getString("user_name"));
        t.setUserEmail(rs.getString("user_email"));
        t.setTechName(rs.getString("tech_name"));
        return t;
    }
}