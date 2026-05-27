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

import java.util.List;

public class SetupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SecurePrefs prefs = new SecurePrefs(this);
        TextInputEditText hostInput = findViewById(R.id.edit_host);
        TextInputEditText apiInput = findViewById(R.id.edit_api_key);
        TextInputEditText userSsh = findViewById(R.id.edit_ssh_user);
        TextInputEditText passSsh = findViewById(R.id.edit_ssh_password);
        MaterialSwitch sslSwitch = findViewById(R.id.switch_self_signed);
        MaterialButton btnConnect = findViewById(R.id.btn_connect);
        MaterialButton btnDemo = findViewById(R.id.btn_demo);

        btnConnect.setOnClickListener(v -> {
            String host = hostInput.getText() != null ? hostInput.getText().toString().trim() : "";
            String key = apiInput.getText() != null ? apiInput.getText().toString().trim() : "";
            boolean ssl = sslSwitch.isChecked();

            if (host.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.saveHost(host);
            prefs.saveApiKey(key);
            prefs.saveAllowSelfSigned(ssl);

            com.dmitrij.behappy.model.ServerProfile profile = prefs.getCurrentProfile();
            if (profile != null) {
                profile.setSshUser(userSsh.getText().toString());
                profile.setSshPassword(passSsh.getText().toString());
                List<com.dmitrij.behappy.model.ServerProfile> list = prefs.getProfiles();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(profile.getId())) {
                        list.set(i, profile);
                        break;
                    }
                }
                prefs.saveProfiles(list);
            }
            
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnDemo.setOnClickListener(v -> {
            prefs.saveHost("demo_mode");
            prefs.saveApiKey("demo_token");
            prefs.saveAllowSelfSigned(false);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
