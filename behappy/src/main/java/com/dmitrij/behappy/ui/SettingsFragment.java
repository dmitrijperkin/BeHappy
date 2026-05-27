package com.dmitrij.behappy.ui;

import android.content.ComponentName;
import android.content.pm.PackageManager;
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
    private SecurePrefs userPreferences;
    private TrueNasRepository dataRepository;
    private TextInputEditText nasHostEditText, apiSecretEditText;
    private MaterialSwitch ignoreSslSwitch, enableBiometricsSwitch;
    private AutoCompleteTextView serverProfileDropdown;
    private List<ServerProfile> serverProfilesList;
    private int aboutClickCount = 0;
    private long lastAboutClickTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View settingsView = inflater.inflate(R.layout.fragment_settings, container, false);
        
        if (getActivity() instanceof AppCompatActivity parentAppCompatActivity) {
            if (parentAppCompatActivity.getSupportActionBar() != null) {
                parentAppCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        userPreferences = new SecurePrefs(requireContext());
        dataRepository = new TrueNasRepository();
        
        serverProfileDropdown = settingsView.findViewById(R.id.profile_selector);
        Button addProfileButton = settingsView.findViewById(R.id.btn_add_profile);
        Button deleteProfileButton = settingsView.findViewById(R.id.btn_delete_profile);

        nasHostEditText = settingsView.findViewById(R.id.input_host);
        apiSecretEditText = settingsView.findViewById(R.id.input_api_key);
        ignoreSslSwitch = settingsView.findViewById(R.id.switch_self_signed);
        enableBiometricsSwitch = settingsView.findViewById(R.id.switch_biometric);
        Button saveSettingsButton = settingsView.findViewById(R.id.btn_save);
        Button systemRebootButton = settingsView.findViewById(R.id.btn_reboot);
        Button systemShutdownButton = settingsView.findViewById(R.id.btn_shutdown);

        refreshProfilesDropdown();

        serverProfileDropdown.setOnItemClickListener((parent, viewElement, position, id) -> {
            ServerProfile selectedProfile = (ServerProfile) parent.getItemAtPosition(position);
            userPreferences.saveCurrentProfileId(selectedProfile.getId());
            loadActiveProfileData();
        });

        addProfileButton.setOnClickListener(v -> displayAddProfileDialog());
        deleteProfileButton.setOnClickListener(v -> displayDeleteConfirmDialog());

        saveSettingsButton.setOnClickListener(v -> {
            if (nasHostEditText.getText() == null || apiSecretEditText.getText() == null) return;

            String nasHostAddress = nasHostEditText.getText().toString().trim();
            String nasApiToken = apiSecretEditText.getText().toString().trim();

            if (nasHostAddress.isEmpty() || nasApiToken.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
                return;
            }

            ServerProfile activeServerProfile = userPreferences.getCurrentProfile();
            if (activeServerProfile != null) {
                activeServerProfile.setHost(nasHostAddress);
                activeServerProfile.setApiKey(nasApiToken);
                activeServerProfile.setAllowSelfSigned(ignoreSslSwitch.isChecked());
                userPreferences.saveProfiles(serverProfilesList);
            }

            userPreferences.setBiometricEnabled(enableBiometricsSwitch.isChecked());
            Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show();
        });

        systemRebootButton.setOnClickListener(v -> displayConfirmPowerDialog(true));
        systemShutdownButton.setOnClickListener(v -> displayConfirmPowerDialog(false));

        settingsView.findViewById(R.id.btn_check_update).setOnClickListener(v -> checkForSystemUpdates());

        settingsView.findViewById(R.id.text_about).setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAboutClickTime > 2000) {
                aboutClickCount = 0;
            }
            aboutClickCount++;
            lastAboutClickTime = currentTime;

            if (aboutClickCount >= 5) {
                aboutClickCount = 0;
                initiateBiometricMaskToggleAuth();
            }
        });

        return settingsView;
    }

    private void initiateBiometricMaskToggleAuth() {
        Executor exec = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt prompt = new BiometricPrompt(this, exec, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                toggleAppMask();
            }
        });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        prompt.authenticate(info);
    }

    private void toggleAppMask() {
        PackageManager pm = requireContext().getPackageManager();
        String pkg = requireContext().getPackageName();
        ComponentName main = new ComponentName(pkg, "com.dmitrij.behappy.ui.SplashActivity");
        ComponentName fake = new ComponentName(pkg, "com.dmitrij.behappy.ui.FakeLauncher");

        boolean isMainEnabled = pm.getComponentEnabledSetting(main) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (isMainEnabled) {
            pm.setComponentEnabledSetting(fake, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(requireContext(), "Masking enabled. Icon will change shortly.", Toast.LENGTH_LONG).show();
        } else {
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(fake, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(requireContext(), "Masking disabled. Original icon restored.", Toast.LENGTH_LONG).show();
        }
    }

    private void checkForSystemUpdates() {
        if (userPreferences.getHost().isEmpty() || userPreferences.getApiKey().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), R.string.msg_check_update, Toast.LENGTH_SHORT).show();

        dataRepository.checkUpdate(requireContext(), userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(),
                updateCheckData -> {
                    if (isAdded()) {
                        String updateStatusString = String.valueOf(updateCheckData.get("status"));
                        if ("AVAILABLE".equals(updateStatusString)) {
                            String availableVersionString = String.valueOf(updateCheckData.get("version"));
                            displayUpdateDialog(availableVersionString);
                        } else {
                            Toast.makeText(requireContext(), R.string.msg_no_updates, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                errorMessage -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void displayUpdateDialog(String availableVersionString) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_update_title)
                .setMessage(getString(R.string.msg_update_found, availableVersionString) + "\n\n" + getString(R.string.dialog_update_message))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_update_now, (dialogInterface, index) -> {
                    if (userPreferences.isBiometricEnabled()) {
                        initiateBiometricUpdateAuth();
                    } else {
                        executeSystemUpdate();
                    }
                })
                .show();
    }

    private void initiateBiometricUpdateAuth() {
        Executor uiThreadExecutor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricAuthPrompt = new BiometricPrompt(this, uiThreadExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult authResult) {
                super.onAuthenticationSucceeded(authResult);
                executeSystemUpdate();
            }
        });

        BiometricPrompt.PromptInfo biometricPromptDetails = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setSubtitle(getString(R.string.dialog_update_title))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricAuthPrompt.authenticate(biometricPromptDetails);
    }

    private void executeSystemUpdate() {
        dataRepository.updateSystem(requireContext(), userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(), new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void refreshProfilesDropdown() {
        serverProfilesList = userPreferences.getProfiles();
        if (serverProfilesList.isEmpty()) {
            ServerProfile defaultServerProfile = new ServerProfile(getString(R.string.default_profile_name), "", "", false);
            serverProfilesList.add(defaultServerProfile);
            userPreferences.saveProfiles(serverProfilesList);
            userPreferences.saveCurrentProfileId(defaultServerProfile.getId());
        }

        ArrayAdapter<ServerProfile> profilesDropdownAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, serverProfilesList);
        serverProfileDropdown.setAdapter(profilesDropdownAdapter);

        ServerProfile activeServerProfile = userPreferences.getCurrentProfile();
        if (activeServerProfile != null) {
            serverProfileDropdown.setText(activeServerProfile.getName(), false);
            loadActiveProfileData();
        }
    }

    private void loadActiveProfileData() {
        ServerProfile activeServerProfile = userPreferences.getCurrentProfile();
        if (activeServerProfile != null) {
            nasHostEditText.setText(activeServerProfile.getHost());
            apiSecretEditText.setText(activeServerProfile.getApiKey());
            ignoreSslSwitch.setChecked(activeServerProfile.isAllowSelfSigned());
        }
        enableBiometricsSwitch.setChecked(userPreferences.isBiometricEnabled());
    }

    private void displayAddProfileDialog() {
        TextInputEditText profileNameEditText = new TextInputEditText(requireContext());
        profileNameEditText.setHint(R.string.label_profile_name);
        
        android.widget.FrameLayout dialogLayoutContainer = new android.widget.FrameLayout(requireContext());
        android.widget.FrameLayout.LayoutParams viewLayoutParams = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        viewLayoutParams.leftMargin = viewLayoutParams.rightMargin = 50;
        profileNameEditText.setLayoutParams(viewLayoutParams);
        dialogLayoutContainer.addView(profileNameEditText);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.btn_add_profile)
                .setView(dialogLayoutContainer)
                .setPositiveButton(android.R.string.ok, (dialogInterface, index) -> {
                    String newProfileName = profileNameEditText.getText() != null ? profileNameEditText.getText().toString().trim() : "";
                    if (!newProfileName.isEmpty()) {
                        ServerProfile freshServerProfile = new ServerProfile(newProfileName, "", "", false);
                        serverProfilesList.add(freshServerProfile);
                        userPreferences.saveProfiles(serverProfilesList);
                        userPreferences.saveCurrentProfileId(freshServerProfile.getId());
                        refreshProfilesDropdown();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void displayDeleteConfirmDialog() {
        ServerProfile activeServerProfile = userPreferences.getCurrentProfile();
        if (activeServerProfile == null || serverProfilesList.size() <= 1) {
            Toast.makeText(requireContext(), R.string.err_cannot_delete_only_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_profile_title)
                .setMessage(R.string.dialog_delete_profile_message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, index) -> {
                    serverProfilesList.remove(activeServerProfile);
                    userPreferences.saveProfiles(serverProfilesList);
                    if (!serverProfilesList.isEmpty()) {
                        userPreferences.saveCurrentProfileId(serverProfilesList.get(0).getId());
                    }
                    refreshProfilesDropdown();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void displayConfirmPowerDialog(boolean shouldPerformReboot) {
        String dialogTitleString = getString(shouldPerformReboot ? R.string.dialog_reboot_title : R.string.dialog_shutdown_title);
        String dialogMessageString = getString(shouldPerformReboot ? R.string.dialog_reboot_message : R.string.dialog_shutdown_message);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(dialogTitleString)
                .setMessage(dialogMessageString)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialogInterface, index) -> {
                    if (userPreferences.isBiometricEnabled()) {
                        initiateBiometricPowerAuth(shouldPerformReboot);
                    } else {
                        executePowerAction(shouldPerformReboot);
                    }
                })
                .show();
    }

    private void initiateBiometricPowerAuth(boolean shouldPerformReboot) {
        Executor uiThreadExecutor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricAuthPrompt = new BiometricPrompt(this, uiThreadExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int authErrorCode, @NonNull CharSequence authErrorDescription) {
                super.onAuthenticationError(authErrorCode, authErrorDescription);
                if (authErrorCode != BiometricPrompt.ERROR_USER_CANCELED && authErrorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(requireContext(), authErrorDescription, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult authResult) {
                super.onAuthenticationSucceeded(authResult);
                executePowerAction(shouldPerformReboot);
            }
        });

        BiometricPrompt.PromptInfo biometricPromptDetails = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setSubtitle(getString(shouldPerformReboot ? R.string.dialog_reboot_title : R.string.dialog_shutdown_title))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricAuthPrompt.authenticate(biometricPromptDetails);
    }

    private void executePowerAction(boolean shouldPerformReboot) {
        if (userPreferences.getHost().isEmpty() || userPreferences.getApiKey().isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_missing_settings, Toast.LENGTH_SHORT).show();
            return;
        }

        TrueNasRepository.ActionCallback powerActionResultListener = new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show());
                }
            }
        };

        if (shouldPerformReboot) {
            dataRepository.rebootSystem(requireContext(), userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(), powerActionResultListener);
        } else {
            dataRepository.shutdownSystem(requireContext(), userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(), powerActionResultListener);
        }
    }

    @Override
    public void onDestroyView() {
        if (getActivity() instanceof AppCompatActivity parentAppCompatActivity) {
            if (parentAppCompatActivity.getSupportActionBar() != null) {
                parentAppCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        super.onDestroyView();
    }
}
