package com.dmitrij.behappy.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.concurrent.Executor;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SecurePrefs prefs = new SecurePrefs(this);
            if (prefs.getHost().isEmpty() || prefs.getApiKey().isEmpty()) {
                startActivity(new Intent(this, SetupActivity.class));
                finish();
            } else if (prefs.isBiometricEnabled()) {
                auth();
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }, 1000);
    }

    private void auth() {
        Executor exec = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(SplashActivity.this,
                exec, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int code, @NonNull CharSequence err) {
                super.onAuthenticationError(code, err);
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                if (code == BiometricPrompt.ERROR_NEGATIVE_BUTTON || code == BiometricPrompt.ERROR_USER_CANCELED) {
                    finish();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult res) {
                super.onAuthenticationSucceeded(res);
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_required))
                .setSubtitle(getString(R.string.auth_reason))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                          androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        
        prompt.authenticate(info);
    }
}
