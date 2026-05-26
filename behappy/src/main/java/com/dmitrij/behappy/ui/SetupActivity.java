package com.dmitrij.behappy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.dmitrij.behappy.R;
import com.dmitrij.behappy.security.SecurePrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SecurePrefs prefs = new SecurePrefs(this);
        TextInputEditText editHost = findViewById(R.id.edit_host);
        TextInputEditText editApiKey = findViewById(R.id.edit_api_key);
        MaterialSwitch switchSelfSigned = findViewById(R.id.switch_self_signed);
        MaterialButton btnConnect = findViewById(R.id.btn_connect);
        MaterialButton btnDemo = findViewById(R.id.btn_demo);

        btnConnect.setOnClickListener(v -> {
            String host = editHost.getText() != null ? editHost.getText().toString().trim() : "";
            String apiKey = editApiKey.getText() != null ? editApiKey.getText().toString().trim() : "";
            boolean allowSelfSigned = switchSelfSigned.isChecked();

            if (host.isEmpty() || apiKey.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.saveHost(host);
            prefs.saveApiKey(apiKey);
            prefs.saveAllowSelfSigned(allowSelfSigned);
            
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnDemo.setOnClickListener(v -> {
            prefs.saveHost("demo_mode");
            prefs.saveApiKey("demo_token");
            prefs.saveAllowSelfSigned(false);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
