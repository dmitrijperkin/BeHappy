package com.dmitrij.behappy.model;

public class UsageInfo {
    private final double cpuPercent;
    private final double ramPercent;
    private String cpuModel;
    private long totalMemory;

    public UsageInfo(double cpuPercent, double ramPercent) {
        this.cpuPercent = cpuPercent;
        this.ramPercent = ramPercent;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }
    public double getRamPercent() {
        return ramPercent;
    }

    public String getCpuModel() {
        return cpuModel;
    }
    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public long getTotalMemory() {
        return totalMemory;
    }
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }
}
