package com.stackroute.helpdesk.dto;

/**
 * Java 17 Record modeling aggregate ticket metrics and SLA performance indicators.
 */
public record TicketStatisticsDTO(
    long totalCount,
    long openCount,
    long inProgressCount,
    long resolvedCount,
    long closedCount,
    long breachedCount,
    long unassignedCount
) {
    public static TicketStatisticsDTO empty() {
        return new TicketStatisticsDTO(0, 0, 0, 0, 0, 0, 0);
    }
}