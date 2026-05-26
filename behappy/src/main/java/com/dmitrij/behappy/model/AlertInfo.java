package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class AlertInfo {
    @SerializedName("level")
    private String level;

    @SerializedName("formatted")
    private String formatted;

    public String getLevel() {
        return level == null ? "INFO" : level;
    }

    public String getFormatted() {
        return formatted == null ? "-" : formatted;
    }
}
