package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class AuditEntry {
    @SerializedName("service")
    private String service;
    
    @SerializedName("user")
    private String user;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    @SerializedName("event")
    private String event;
    
    @SerializedName("event_data")
    private String eventData;

    public AuditEntry() {}

    public void setService(String s) { this.service = s; }
    public void setUser(String u) { this.user = u; }
    public void setTimestamp(String t) { this.timestamp = t; }
    public void setEvent(String e) { this.event = e; }
    public void setEventData(String ed) { this.eventData = ed; }
    public String getService() { return service; }
    public String getUser() { return user; }
    public String getTimestamp() { return timestamp; }
    public String getEvent() { return event; }
    public String getEventData() { return eventData; }
}
