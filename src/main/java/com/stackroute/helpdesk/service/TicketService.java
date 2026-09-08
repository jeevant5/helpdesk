package com.stackroute.helpdesk.service;

import com.stackroute.helpdesk.dto.TicketSearchCriteria;
import com.stackroute.helpdesk.dto.TicketStatisticsDTO;
import com.stackroute.helpdesk.model.Ticket;
import com.stackroute.helpdesk.model.User;
import com.stackroute.helpdesk.util.ServiceResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TicketService {
    ServiceResult<Ticket> createTicket(Ticket ticket, InputStream attachmentStream, long attachmentSize);
    Optional<Ticket> getTicketById(int ticketId);
    List<Ticket> getTicketsByCriteria(TicketSearchCriteria criteria);
    List<Ticket> getUnassignedTickets();
    List<Ticket> getTicketsAssignedToTech(int techId);
    List<Ticket> getAllAssignedTickets();
    TicketStatisticsDTO getTicketStatistics();
    Map<String, Integer> getStatusCounts();
    ServiceResult<Void> assignTicket(int ticketId, int techId);
    ServiceResult<Void> updateStatus(int ticketId, String newStatus);
    boolean writeAttachment(int ticketId, OutputStream out);
    String generateCsvReport(TicketSearchCriteria criteria);
    ServiceResult<Void> deleteTicket(int ticketId, User currentUser);
    ServiceResult<Integer> deleteAllTickets(User currentUser);
}