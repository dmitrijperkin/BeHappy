package com.dmitrij.behappy.model;

import com.dmitrij.behappy.ui.VmAdapter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class IxAppInfo implements VmAdapter.Displayable {
    @SerializedName("name")
    private JsonElement name;
    @SerializedName("state")
    private JsonElement state;
    @SerializedName("version")
    private JsonElement version;
    @SerializedName(value = "upgrade_available", alternate = {"update_available"})
    private boolean upgradeAvailable;
    @SerializedName("metadata")
    private JsonObject metadata;

    public String getName() {
        return asText(name, "app");
    }
    public String getState() {
        return asText(state, "UNKNOWN");
    }
    public String getVersion() {
        return asText(version, "-");
    }
    public boolean isUpgradeAvailable() {
        return upgradeAvailable;
    }

    private String asText(JsonElement element, String fallback) {
        if (element == null || element.isJsonNull()) return fallback;
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("value") && obj.get("value").isJsonPrimitive()) return obj.get("value").getAsString();
            if (obj.has("name") && obj.get("name").isJsonPrimitive()) return obj.get("name").getAsString();
        }
        return fallback;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getDisplayStatus() {
        return getState();
    }

    @Override
    public String getDisplayInfo() {
        if (metadata != null && metadata.has("chart_metadata") && metadata.get("chart_metadata").isJsonObject()) {
            JsonObject chart = metadata.getAsJsonObject("chart_metadata");
            if (chart.has("appVersion") && chart.get("appVersion").isJsonPrimitive()) {
                return "Version: " + chart.get("appVersion").getAsString();
            }
        }
        return "Version: " + getVersion();
    }

    @Override
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(getState());
    }

    @Override
    public boolean hasUpdate() {
        return isUpgradeAvailable();
    }
}
