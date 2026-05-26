package com.dmitrij.behappy.model;

import java.util.UUID;

public class ServerProfile {
    private String id;
    private String name;
    private String host;
    private String apiKey;
    private boolean allowSelfSigned;

    public ServerProfile(String name, String host, String apiKey, boolean allowSelfSigned) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.host = host;
        this.apiKey = apiKey;
        this.allowSelfSigned = allowSelfSigned;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public boolean isAllowSelfSigned() { return allowSelfSigned; }
    public void setAllowSelfSigned(boolean allowSelfSigned) { this.allowSelfSigned = allowSelfSigned; }

    @Override
    public String toString() {
        return name;
    }
}
