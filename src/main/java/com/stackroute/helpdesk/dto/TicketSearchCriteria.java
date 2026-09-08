package com.stackroute.helpdesk.dto;

/**
 * Java 17 Record encapsulating multi-criteria ticket query parameters.
 */
public record TicketSearchCriteria(
    String keyword,
    String status,
    String priority,
    Integer userId,
    boolean isTechnician
) {
    public TicketSearchCriteria {
        keyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        status = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : "ALL";
        priority = (priority != null && !priority.isBlank()) ? priority.trim().toUpperCase() : "ALL";
    }

    public static TicketSearchCriteria of(String keyword, String status, String priority, Integer userId, boolean isTechnician) {
        return new TicketSearchCriteria(keyword, status, priority, userId, isTechnician);
    }
}