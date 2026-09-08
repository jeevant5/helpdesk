package com.stackroute.helpdesk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO entity for aggregate ticket counts grouped by status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusCount implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;
    private int count;
}