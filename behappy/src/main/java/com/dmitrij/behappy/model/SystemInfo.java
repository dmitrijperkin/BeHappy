package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class SystemInfo {
    @SerializedName("version")
    private String version;

    @SerializedName("uptime")
    private String uptime;

    @SerializedName("model")
    private String cpuModel;

    @SerializedName("physmem")
    private long totalMemory;

    public String getVersion() {
        return version == null ? "unknown" : version;
    }

    public String getUptime() {
        return uptime == null ? "unknown" : uptime;
    }

    public String getCpuModel() {
        return cpuModel == null ? "unknown" : cpuModel;
    }

    public long getTotalMemory() {
        return totalMemory;
    }
}
