package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class SmbShare {
    @SerializedName("id")
    private int id;
    @SerializedName("path")
    private String path;
    @SerializedName("name")
    private String name;
    @SerializedName("enabled")
    private boolean enabled;
    @SerializedName("comment")
    private String comment;

    public int getId() {
        return id;
    }
    public String getPath() {
        return path;
    }
    public String getName() {
        return name == null ? "SMB Share" : name;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public String getComment() {
        return comment;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
