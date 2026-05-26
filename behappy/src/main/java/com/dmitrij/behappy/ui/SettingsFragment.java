package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.ServerProfile;
import com.dmitrij.behappy.security.SecurePrefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.Executor;

public class SettingsFragment extends Fragment {
    private SecurePrefs prefs;
    private TrueNasRepository repository;
    private TextInputEditText hostInput, apiInput;
    private MaterialSwitch selfSignedSwitch, biometricSwitch;
    private AutoCompleteTextView profileSelector;
    private List<ServerProfile> profiles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        if (getActivity() instanceof AppCompatActivity activity) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        prefs = new SecurePrefs(requireContext());
        repository = new TrueNasRepository();
        
        profileSelector = view.findViewById(R.id.profile_selector);
        Button addProfileBtn = view.findViewById(R.id.btn_add_profile);
        Button deleteProfileBtn = view.findViewById(R.id.btn_delete_profile);

        hostInput = view.findViewById(R.id.input_host);
        apiInput = view.findViewById(R.id.input_api_key);
        selfSignedSwitch = view.findViewById(R.id.switch_self_signed);
        biometricSwitch = view.findViewById(R.id.switch_biometric);
        Button saveBtn = view.findViewById(R.id.btn_save);
        Button rebootBtn = view.findViewById(R.id.btn_reboot);
        Button shutdownBtn = view.findViewById(R.id.btn_shutdown);

        refreshProfiles();

        profileSelector.setOnItemClickListener((parent, v, position, id) -> {
            ServerProfile selected = (ServerProfile) parent.getItemAtPosition(position);
            prefs.saveCurrentProfileId(selected.getId());
            loadCurrentProfile();
        });

        addProfileBtn.setOnClickListener(v -> showAddProfileDialog());
        deleteProfileBtn.setOnClickListener(v -> showDeleteConfirmDialog());

        saveBtn.setOnClickListener(v -> {
            if (hostInput.getText() == null || apiInput.getText() == null) return;

            String host = hostInput.getText().toString().trim();
            String apiKey = apiInput.getText().toString().trim();

            if (host.isEmpty() || apiKey.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
                return;
            }

            ServerProfile current = prefs.getCurrentProfile();
            if (current != null) {
                current.setHost(host);
                current.setApiKey(apiKey);
                current.setAllowSelfSigned(selfSignedSwitch.isChecked());
                prefs.saveProfiles(profiles);
            }

            prefs.setBiometricEnabled(biometricSwitch.isChecked());
            Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show();
        });

        rebootBtn.setOnClickListener(v -> showConfirmDialog(true));
        shutdownBtn.setOnClickListener(v -> showConfirmDialog(false));

        view.findViewById(R.id.btn_check_update).setOnClickListener(v -> checkUpdate());

        return view;
    }

    private void checkUpdate() {
        if (prefs.getHost().isEmpty() || prefs.getApiKey().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), R.string.msg_check_update, Toast.LENGTH_SHORT).show();

        repository.checkUpdate(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(),
                data -> {
                    if (isAdded()) {
                        String status = String.valueOf(data.get("status"));
                        if ("AVAILABLE".equals(status)) {
                            String version = String.valueOf(data.get("version"));
                            showUpdateDialog(version);
                        } else {
                            Toast.makeText(requireContext(), R.string.msg_no_updates, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                msg -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showUpdateDialog(String version) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_update_title)
                .setMessage(getString(R.string.msg_update_found, version) + "\n\n" + getString(R.string.dialog_update_message))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_update_now, (dialog, which) -> {
                    if (prefs.isBiometricEnabled()) {
                        authenticateForUpdate();
                    } else {
                        performUpdate();
                    }
                })
                .show();
    }

    private void authenticateForUpdate() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                performUpdate();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setSubtitle(getString(R.string.dialog_update_title))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void performUpdate() {
        repository.updateSystem(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void refreshProfiles() {
        profiles = prefs.getProfiles();
        if (profiles.isEmpty()) {
            ServerProfile def = new ServerProfile(getString(R.string.default_profile_name), "", "", false);
            profiles.add(def);
            prefs.saveProfiles(profiles);
            prefs.saveCurrentProfileId(def.getId());
        }

        ArrayAdapter<ServerProfile> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, profiles);
        profileSelector.setAdapter(adapter);

        ServerProfile current = prefs.getCurrentProfile();
        if (current != null) {
            profileSelector.setText(current.getName(), false);
            loadCurrentProfile();
        }
    }

    private void loadCurrentProfile() {
        ServerProfile current = prefs.getCurrentProfile();
        if (current != null) {
            hostInput.setText(current.getHost());
            apiInput.setText(current.getApiKey());
            selfSignedSwitch.setChecked(current.isAllowSelfSigned());
        }
        biometricSwitch.setChecked(prefs.isBiometricEnabled());
    }

    private void showAddProfileDialog() {
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint(R.string.label_profile_name);
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.btn_add_profile)
                .setView(container)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!name.isEmpty()) {
                        ServerProfile newProfile = new ServerProfile(name, "", "", false);
                        profiles.add(newProfile);
                        prefs.saveProfiles(profiles);
                        prefs.saveCurrentProfileId(newProfile.getId());
                        refreshProfiles();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmDialog() {
        ServerProfile current = prefs.getCurrentProfile();
        if (current == null || profiles.size() <= 1) {
            Toast.makeText(requireContext(), R.string.err_cannot_delete_only_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_profile_title)
                .setMessage(R.string.dialog_delete_profile_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    profiles.remove(current);
                    prefs.saveProfiles(profiles);
                    if (!profiles.isEmpty()) {
                        prefs.saveCurrentProfileId(profiles.get(0).getId());
                    }
                    refreshProfiles();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showConfirmDialog(boolean reboot) {
        String title = getString(reboot ? R.string.dialog_reboot_title : R.string.dialog_shutdown_title);
        String message = getString(reboot ? R.string.dialog_reboot_message : R.string.dialog_shutdown_message);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    if (prefs.isBiometricEnabled()) {
                        authenticateForAction(reboot);
                    } else {
                        performPowerAction(reboot);
                    }
                })
                .show();
    }

    private void authenticateForAction(boolean reboot) {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(requireContext(), errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                performPowerAction(reboot);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setSubtitle(getString(reboot ? R.string.dialog_reboot_title : R.string.dialog_shutdown_title))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void performPowerAction(boolean reboot) {
        if (prefs.getHost().isEmpty() || prefs.getApiKey().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
            return;
        }

        TrueNasRepository.ActionCallback callback = new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
                }
            }
        };

        if (reboot) {
            repository.rebootSystem(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), callback);
        } else {
            repository.shutdownSystem(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), callback);
        }
    }

    @Override
    public void onDestroyView() {
        if (getActivity() instanceof AppCompatActivity activity) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        super.onDestroyView();
    }
}
