package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;

public class DatasetInfo {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("pool")
    private String pool;

    @SerializedName("type")
    private String type;

    @SerializedName("used")
    private SpaceValue used;

    @SerializedName("available")
    private SpaceValue available;

    public static class SpaceValue {
        @SerializedName("raw")
        private Long raw;

        @SerializedName("parsed")
        private Long parsed;

        @SerializedName("value")
        private String value;

        public Long getRaw() {
            return raw == null ? 0L : raw;
        }
        public Long getParsed() {
            return parsed;
        }
        public String getValue() {
            return value;
        }
    }

    public String getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public String getPool() {
        return pool;
    }

    public String getType() {
        return type;
    }

    public SpaceValue getUsed() {
        return used;
    }

    public SpaceValue getAvailable() {
        return available;
    }
}