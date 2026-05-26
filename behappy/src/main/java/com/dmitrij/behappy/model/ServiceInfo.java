package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class ServiceInfo {
    @SerializedName("service")
    private String service;

    @SerializedName("state")
    private String state;

    @SerializedName("id")
    private Integer id;

    public Integer getId() {
        return id;
    }

    public String getService() {
        return service == null ? "unknown" : service;
    }

    public String getState() {
        return state == null ? "unknown" : state;
    }
}
