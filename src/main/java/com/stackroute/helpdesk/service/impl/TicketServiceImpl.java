package com.stackroute.helpdesk.service.impl;

import com.stackroute.helpdesk.dao.TicketDAO;
import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.dto.TicketStatisticsDTO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.service.TicketService;
import com.stackroute.helpdesk.util.ServiceResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {

    private final TicketDAO ticketDAO;

    public TicketServiceImpl() {
        this.ticketDAO = new TicketDAO();
    }

    public TicketServiceImpl(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    @Override
    public ServiceResult<Ticket> createTicket(Ticket ticket, InputStream attachmentStream, long attachmentSize) {
        if (ticket == null) {
            return ServiceResult.fail("VALIDATION_ERROR", "Ticket payload cannot be null.");
        }
        if (ticket.getTitle() == null || ticket.getTitle().isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Ticket subject / title is required.");
        }
        if (ticket.getDescription() == null || ticket.getDescription().isBlank()) {
            return ServiceResult.fail("VALIDATION_ERROR", "Detailed issue description is required.");
        }

        String priority = Optional.ofNullable(ticket.getPriority())
            .map(String::trim)
            .map(String::toUpperCase)
            .filter(p -> List.of("HIGH", "MEDIUM", "LOW").contains(p))
            .orElse("MEDIUM");
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");

        try {
            int ticketId = ticketDAO.createTicket(ticket, attachmentStream, attachmentSize);
            if (ticketId > 0) {
                ticket.setTicketId(ticketId);
                return ServiceResult.ok(ticket, "Support ticket #" + ticketId + " logged successfully.");
            }
            return ServiceResult.fail("DB_ERROR", "Failed to insert ticket into database.");
        } catch (SQLException e) {
            return ServiceResult.fail("DB_ERROR", "Database error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Ticket> getTicketById(int ticketId) {
        if (ticketId <= 0) return Optional.empty();
        return Optional.ofNullable(ticketDAO.getTicketById(ticketId));
    }

    @Override
    public List<Ticket> getTicketsByCriteria(TicketSearchCriteria criteria) {
        if (criteria == null) {
            return ticketDAO.getAllTickets();
        }
        int userId = criteria.userId() != null ? criteria.userId() : 0;
        return ticketDAO.searchTickets(
            criteria.keyword(),
            criteria.status(),
            criteria.priority(),
            userId,
            criteria.isTechnician()
        );
    }

    @Override
    public List<Ticket> getUnassignedTickets() {
        // Fetch unassigned tickets and sort by urgency (earliest deadline first) using Streams
        return ticketDAO.getUnassignedTickets().stream()
            .sorted(Comparator.comparingLong(Ticket::getSlaDeadlineMillis))
            .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> getTicketsAssignedToTech(int techId) {
        if (techId <= 0) return Collections.emptyList();
        return ticketDAO.getTicketsAssignedToTech(techId).stream()
            .sorted(Comparator.comparingLong(Ticket::getSlaDeadlineMillis))
            .collect(Collectors.toList());
    }

    @Override
    public TicketStatisticsDTO getTicketStatistics() {
        List<Ticket> allTickets = ticketDAO.getAllTickets();

        // Modern Stream processing for aggregate metrics
        Map<String, Long> statusCounts = allTickets.stream()
            .collect(Collectors.groupingBy(
                t -> Optional.ofNullable(t.getStatus()).map(String::toUpperCase).orElse("UNKNOWN"),
                Collectors.counting()
            ));

        long breachedCount = allTickets.stream()
            .filter(Ticket::isSlaBreached)
            .count();

        long unassignedCount = allTickets.stream()
            .filter(t -> t.getTechId() == null && "OPEN".equalsIgnoreCase(t.getStatus()))
            .count();

        return new TicketStatisticsDTO(
            allTickets.size(),
            statusCounts.getOrDefault("OPEN", 0L),
            statusCounts.getOrDefault("IN_PROGRESS", 0L),
            statusCounts.getOrDefault("RESOLVED", 0L),
            statusCounts.getOrDefault("CLOSED", 0L),
            breachedCount,
            unassignedCount
        );
    }

    @Override
    public Map<String, Integer> getStatusCounts() {
        return ticketDAO.getStatusCounts();
    }

    @Override
    public ServiceResult<Void> assignTicket(int ticketId, int techId) {
        if (ticketId <= 0 || techId <= 0) {
            return ServiceResult.fail("INVALID_ID", "Valid ticket ID and technician ID are required.");
        }

        boolean success = ticketDAO.assignTicket(ticketId, techId);
        if (success) {
            return ServiceResult.ok(null, "Ticket successfully claimed and assigned.");
        }
        return ServiceResult.fail("ASSIGN_FAILED", "Could not assign ticket. It may already be assigned.");
    }

    @Override
    public ServiceResult<Void> updateStatus(int ticketId, String newStatus) {
        if (ticketId <= 0 || newStatus == null || newStatus.isBlank()) {
            return ServiceResult.fail("INVALID_STATUS", "Valid ticket ID and status are required.");
        }

        boolean success = ticketDAO.updateStatus(ticketId, newStatus.trim().toUpperCase());
        if (success) {
            return ServiceResult.ok(null, "Ticket status updated to " + newStatus);
        }
        return ServiceResult.fail("UPDATE_FAILED", "Failed to update ticket status in database.");
    }

    @Override
    public boolean writeAttachment(int ticketId, OutputStream out) {
        return ticketDAO.writeAttachment(ticketId, out);
    }

    @Override
    public String generateCsvReport(TicketSearchCriteria criteria) {
        List<Ticket> tickets = getTicketsByCriteria(criteria);

        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Microsoft Excel compatibility
        csv.append("\uFEFFTicket ID,Title,Priority,Status,SLA Status,Created By,Assigned Technician,Created Date\r\n");

        // Transform collection to CSV lines using Streams and Lambdas
        String rows = tickets.stream()
            .map(t -> String.format(
                "%d,\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",\"%s\"",
                t.getTicketId(),
                escapeCsv(t.getTitle()),
                t.getPriority(),
                t.getStatus(),
                t.getSlaStatusText(),
                escapeCsv(Optional.ofNullable(t.getUserName()).orElse("N/A")),
                escapeCsv(Optional.ofNullable(t.getTechName()).orElse("Unassigned")),
                Optional.ofNullable(t.getCreatedAt()).map(Object::toString).orElse("")
            ))
            .collect(Collectors.joining("\r\n"));

        csv.append(rows);
        return csv.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    @Override
    public ServiceResult<Void> deleteTicket(int ticketId, User currentUser) {
        if (currentUser == null) {
            return ServiceResult.fail("UNAUTHORIZED", "Authentication required to delete tickets.");
        }
        if (ticketId <= 0) {
            return ServiceResult.fail("INVALID_ID", "Valid ticket ID is required.");
        }

        Ticket ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null) {
            return ServiceResult.fail("NOT_FOUND", "Ticket not found.");
        }
        // Only administrators can delete tickets
        if (!currentUser.isAdmin()) {
            return ServiceResult.fail("FORBIDDEN", "Only administrators have permission to delete tickets.");
        }

        boolean deleted = ticketDAO.deleteTicket(ticketId);
        if (deleted) {
            return ServiceResult.ok(null, "Ticket #" + ticketId + " has been deleted.");
        }
        return ServiceResult.fail("DELETE_FAILED", "Failed to delete ticket from database.");
    }

    @Override
    public ServiceResult<Integer> deleteAllTickets(User currentUser) {
        if (currentUser == null) {
            return ServiceResult.fail("UNAUTHORIZED", "Authentication required to purge tickets.");
        }
        // Only administrators can purge all tickets
        if (!currentUser.isAdmin()) {
            return ServiceResult.fail("FORBIDDEN", "Only administrators have permission to purge all tickets.");
        }

        int count = ticketDAO.deleteAllTickets();
        return ServiceResult.ok(count, "All tickets (" + count + ") have been successfully purged.");
    }
}