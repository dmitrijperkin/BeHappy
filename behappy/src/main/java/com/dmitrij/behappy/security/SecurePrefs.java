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

public class SecurePrefs {
    private static final String FILE_NAME = "secure_prefs";
    private static final String KEY_HOST = "host";
    private static final String KEY_API = "api";
    private static final String KEY_ALLOW_SELF_SIGNED = "allow_self_signed";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_PROFILES = "profiles_json";
    private static final String KEY_CURRENT_PROFILE_ID = "current_profile_id";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public SecurePrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize secure prefs", exception);
        }
    }

    public List<ServerProfile> getProfiles() {
        String json = prefs.getString(KEY_PROFILES, null);
        if (json == null) {
            List<ServerProfile> list = new ArrayList<>();
            String legacyHost = prefs.getString(KEY_HOST, "");
            String legacyApi = prefs.getString(KEY_API, "");
            boolean legacySelfSigned = prefs.getBoolean(KEY_ALLOW_SELF_SIGNED, false);
            
            if (!legacyHost.isEmpty()) {
                ServerProfile p = new ServerProfile("Default", legacyHost, legacyApi, legacySelfSigned);
                list.add(p);
                saveProfiles(list);
                saveCurrentProfileId(p.getId());
            }
            return list;
        }
        return gson.fromJson(json, new TypeToken<List<ServerProfile>>() {}.getType());
    }

    public void saveProfiles(List<ServerProfile> profiles) {
        prefs.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply();
    }

    public String getCurrentProfileId() {
        return prefs.getString(KEY_CURRENT_PROFILE_ID, "");
    }

    public void saveCurrentProfileId(String id) {
        prefs.edit().putString(KEY_CURRENT_PROFILE_ID, id).apply();
    }

    public ServerProfile getCurrentProfile() {
        List<ServerProfile> profiles = getProfiles();
        String currentId = getCurrentProfileId();
        for (ServerProfile p : profiles) {
            if (p.getId().equals(currentId)) return p;
        }
        if (!profiles.isEmpty()) return profiles.get(0);
        return null;
    }

    public void saveHost(String host) {
        prefs.edit().putString(KEY_HOST, host).apply();
        updateCurrentProfile();
    }

    public String getHost() {
        ServerProfile current = getCurrentProfile();
        if (current != null) return current.getHost();
        return prefs.getString(KEY_HOST, "");
    }

    public void saveApiKey(String apiKey) {
        prefs.edit().putString(KEY_API, apiKey).apply();
        updateCurrentProfile();
    }

    public String getApiKey() {
        ServerProfile current = getCurrentProfile();
        if (current != null) return current.getApiKey();
        return prefs.getString(KEY_API, "");
    }

    public void saveAllowSelfSigned(boolean value) {
        prefs.edit().putBoolean(KEY_ALLOW_SELF_SIGNED, value).apply();
        updateCurrentProfile();
    }

    public boolean isAllowSelfSigned() {
        ServerProfile current = getCurrentProfile();
        if (current != null) return current.isAllowSelfSigned();
        return prefs.getBoolean(KEY_ALLOW_SELF_SIGNED, false);
    }

    private void updateCurrentProfile() {
        ServerProfile current = getCurrentProfile();
        if (current == null) return;
        
        List<ServerProfile> profiles = getProfiles();
        for (ServerProfile p : profiles) {
            if (p.getId().equals(current.getId())) {
                p.setHost(prefs.getString(KEY_HOST, p.getHost()));
                p.setApiKey(prefs.getString(KEY_API, p.getApiKey()));
                p.setAllowSelfSigned(prefs.getBoolean(KEY_ALLOW_SELF_SIGNED, p.isAllowSelfSigned()));
                break;
            }
        }
        saveProfiles(profiles);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }
}
