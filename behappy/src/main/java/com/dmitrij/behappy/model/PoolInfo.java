package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PoolInfo {
    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name == null ? "unknown" : name;
    }

    @SerializedName("healthy")
    private Boolean healthy;

    @SerializedName("status")
    private String status;

    @SerializedName("size")
    private Long size;

    @SerializedName("free")
    private Long free;

    @SerializedName("allocated")
    private Long allocated;

    private List<DatasetInfo> datasets = new ArrayList<>();

    public String getStatus() {
        return status == null ? "UNKNOWN" : status;
    }

    public Long getSize() {
        return size == null ? 0L : size;
    }

    public Long getFree() {
        return free == null ? 0L : free;
    }

    public List<DatasetInfo> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetInfo> datasets) {
        this.datasets = datasets;
    }

    public String getHealthLabel() {
        if (status != null) return status;
        return Boolean.TRUE.equals(healthy) ? "HEALTHY" : "CHECK";
    }
}
