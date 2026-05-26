package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class DiskInfo {
    @SerializedName("name")
    private String name;
    @SerializedName("serial")
    private String serial;
    @SerializedName("model")
    private String model;
    @SerializedName("size")
    private long size;
    @SerializedName("type")
    private String type;
    
    private Integer temperature;

    public String getName() {
        return name == null ? "disk" : name;
    }
    public String getSerial() {
        return serial == null ? "-" : serial;
    }
    public String getModel() {
        return model == null ? "Unknown" : model;
    }
    public long getSize() {
        return size;
    }
    public String getType() {
        return type == null ? "HDD" : type;
    }
    
    public Integer getTemperature() {
        return temperature;
    }
    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }
}
