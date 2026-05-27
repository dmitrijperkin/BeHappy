package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class AlertInfo {
    @SerializedName("level")
    private String level;

    @SerializedName("formatted")
    private String formatted;

    public void setLevel(String level) { this.level = level; }
    public void setFormatted(String formatted) { this.formatted = formatted; }

    public String getLevel() {
        return level == null ? "INFO" : level;
    }

    public String getFormatted() {
        return formatted == null ? "-" : formatted;
    }
}
