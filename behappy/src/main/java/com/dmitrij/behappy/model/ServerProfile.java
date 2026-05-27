package com.dmitrij.behappy.model;

import java.util.UUID;

public class ServerProfile {
    private String profileIdentifier;
    private String profileName;
    private String serverAddress;
    private String apiToken;
    private boolean ignoreSslErrors;
    private String sshUsername;
    private String sshSecret;
    private int sshPortNumber = 22;

    public ServerProfile(String initialName, String initialHost, String initialToken, boolean shouldIgnoreSsl) {
        profileIdentifier = UUID.randomUUID().toString();
        profileName = initialName;
        serverAddress = initialHost;
        apiToken = initialToken;
        ignoreSslErrors = shouldIgnoreSsl;
        sshUsername = "root";
        sshSecret = "";
    }

    public String getId() { return profileIdentifier; }
    public String getName() { return profileName; }
    public void setName(String newName) { profileName = newName; }
    public String getHost() { return serverAddress; }
    public void setHost(String newHost) { serverAddress = newHost; }
    public String getApiKey() { return apiToken; }
    public void setApiKey(String newToken) { apiToken = newToken; }
    public boolean isAllowSelfSigned() { return ignoreSslErrors; }
    public void setAllowSelfSigned(boolean newState) { ignoreSslErrors = newState; }
    public String getSshUser() { return sshUsername; }
    public void setSshUser(String newUser) { sshUsername = newUser; }
    public String getSshPassword() { return sshSecret; }
    public void setSshPassword(String newSecret) { sshSecret = newSecret; }
    public int getSshPort() { return sshPortNumber; }
    public void setSshPort(int newPort) { sshPortNumber = newPort; }

    @Override
    public String toString() {
        return profileName;
    }
}
