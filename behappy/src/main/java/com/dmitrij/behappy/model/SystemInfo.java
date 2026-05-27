package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class SystemInfo {
    @SerializedName("version")
    private String softwareVersion;

    @SerializedName("uptime")
    private String systemUptimeSeconds;

    @SerializedName("model")
    private String processorModelName;

    @SerializedName("physmem")
    private long physicalMemoryBytes;

    public String getVersion() {
        return softwareVersion == null ? "unknown" : softwareVersion;
    }

    public String getUptime() {
        return systemUptimeSeconds == null ? "unknown" : systemUptimeSeconds;
    }

    public String getCpuModel() {
        return processorModelName == null ? "unknown" : processorModelName;
    }

    public long getTotalMemory() {
        return physicalMemoryBytes;
    }
}
