package com.dmitrij.behappy.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.dmitrij.behappy.model.ServerProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SecurePrefs {
    private static final String FILE_NAME = "secure_prefs";
    private static final String KEY_HOST = "host";
    private static final String KEY_API = "api";
    private static final String KEY_ALLOW_SELF_SIGNED = "allow_self_signed";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_PROFILES = "profiles_json";
    private static final String KEY_CURRENT_PROFILE_ID = "current_profile_id";

    private final SharedPreferences encryptedStorage;
    private final Gson jsonConverter = new Gson();

    public SecurePrefs(Context context) {
        try {
            MasterKey securityKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedStorage = EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    securityKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception initError) {
            throw new IllegalStateException("Failed to initialize secure prefs", initError);
        }
    }

    public List<ServerProfile> getProfiles() {
        String profilesJson = encryptedStorage.getString(KEY_PROFILES, null);
        if (profilesJson == null) {
            List<ServerProfile> profilesList = new ArrayList<>();
            String oldHost = encryptedStorage.getString(KEY_HOST, "");
            String oldApiKey = encryptedStorage.getString(KEY_API, "");
            boolean oldSelfSignedFlag = encryptedStorage.getBoolean(KEY_ALLOW_SELF_SIGNED, false);
            
            if (!oldHost.isEmpty()) {
                ServerProfile legacyProfile = new ServerProfile("Default", oldHost, oldApiKey, oldSelfSignedFlag);
                profilesList.add(legacyProfile);
                saveProfiles(profilesList);
                saveCurrentProfileId(legacyProfile.getId());
            }
            return profilesList;
        }
        return jsonConverter.fromJson(profilesJson, new TypeToken<List<ServerProfile>>() {}.getType());
    }

    public void saveProfiles(List<ServerProfile> serverProfiles) {
        encryptedStorage.edit().putString(KEY_PROFILES, jsonConverter.toJson(serverProfiles)).apply();
    }

    public String getCurrentProfileId() {
        return encryptedStorage.getString(KEY_CURRENT_PROFILE_ID, "");
    }

    public void saveCurrentProfileId(String profileId) {
        encryptedStorage.edit().putString(KEY_CURRENT_PROFILE_ID, profileId).apply();
    }

    public ServerProfile getCurrentProfile() {
        List<ServerProfile> serverProfiles = getProfiles();
        String activeProfileId = getCurrentProfileId();
        for (ServerProfile profile : serverProfiles) {
            if (Objects.equals(profile.getId(), activeProfileId)) return profile;
        }
        if (!serverProfiles.isEmpty()) return serverProfiles.get(0);
        return null;
    }

    public void saveHost(String serverHost) {
        encryptedStorage.edit().putString(KEY_HOST, serverHost).apply();
        refreshActiveProfile();
    }

    public String getHost() {
        ServerProfile activeProfile = getCurrentProfile();
        if (activeProfile != null && activeProfile.getHost() != null) return activeProfile.getHost();
        return encryptedStorage.getString(KEY_HOST, "");
    }

    public void saveApiKey(String apiSecret) {
        encryptedStorage.edit().putString(KEY_API, apiSecret).apply();
        refreshActiveProfile();
    }

    public String getApiKey() {
        ServerProfile activeProfile = getCurrentProfile();
        if (activeProfile != null && activeProfile.getApiKey() != null) return activeProfile.getApiKey();
        return encryptedStorage.getString(KEY_API, "");
    }

    public void saveAllowSelfSigned(boolean allowFlag) {
        encryptedStorage.edit().putBoolean(KEY_ALLOW_SELF_SIGNED, allowFlag).apply();
        refreshActiveProfile();
    }

    public boolean isAllowSelfSigned() {
        ServerProfile activeProfile = getCurrentProfile();
        if (activeProfile != null) return activeProfile.isAllowSelfSigned();
        return encryptedStorage.getBoolean(KEY_ALLOW_SELF_SIGNED, false);
    }

    private void refreshActiveProfile() {
        ServerProfile activeProfile = getCurrentProfile();
        if (activeProfile == null) return;
        
        List<ServerProfile> serverProfiles = getProfiles();
        for (ServerProfile profile : serverProfiles) {
            if (Objects.equals(profile.getId(), activeProfile.getId())) {
                profile.setHost(encryptedStorage.getString(KEY_HOST, profile.getHost()));
                profile.setApiKey(encryptedStorage.getString(KEY_API, profile.getApiKey()));
                profile.setAllowSelfSigned(encryptedStorage.getBoolean(KEY_ALLOW_SELF_SIGNED, profile.isAllowSelfSigned()));
                break;
            }
        }
        saveProfiles(serverProfiles);
    }

    public void setBiometricEnabled(boolean isEnabled) {
        encryptedStorage.edit().putBoolean(KEY_BIOMETRIC_ENABLED, isEnabled).apply();
    }

    public boolean isBiometricEnabled() {
        return encryptedStorage.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }
}
