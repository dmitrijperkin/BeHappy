package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;
import com.dmitrij.behappy.ui.VmAdapter;

public class VmInfo implements VmAdapter.Displayable {
    @SerializedName("name")
    private String name;
    @SerializedName("status")
    private VmStatus status;
    @SerializedName("id")
    private int id;

    public static class VmStatus {
        @SerializedName("state") private String state;
        public String getState() {
            return state == null ? "UNKNOWN" : state;
        }
    }

    public String getName() {
        return name == null ? "vm" : name;
    }
    public String getStatus() {
        return status == null ? "UNKNOWN" : status.getState();
    }
    public int getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getDisplayStatus() {
        return getStatus();
    }

    @Override
    public String getDisplayInfo() {
        return "VM #" + id;
    }

    @Override
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(getStatus());
    }
}
