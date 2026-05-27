package com.dmitrij.behappy.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NetworkInterface {
    private String name;
    private String type;
    
    @SerializedName("link_state")
    private String linkState;
    
    @SerializedName("aliases")
    private List<Alias> aliases;

    public String getName() { return name; }
    public String getType() { return type; }
    public String getLinkState() { return linkState; }
    public List<Alias> getAliases() { return aliases; }
    public void setName(String n) { this.name = n; }
    public void setType(String t) { this.type = t; }
    public void setLinkState(String ls) { this.linkState = ls; }
    public void setAliases(List<Alias> a) { this.aliases = a; }

    public static class Alias {
        private String address;
        private int netmask;

        public Alias(String a, int n) {
            this.address = a;
            this.netmask = n;
        }

        public String getAddress() { return address; }
        public int getNetmask() { return netmask; }
    }
}
