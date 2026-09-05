package com.stackroute.helpdesk.model;

import java.io.Serializable;

public class StatusCount implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;
    private int count;

    public StatusCount() {}

    public StatusCount(String status, int count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}