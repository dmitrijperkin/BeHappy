package com.dmitrij.behappy.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.ServerProfile;
import com.dmitrij.behappy.security.SecurePrefs;
import com.google.android.material.button.MaterialButton;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SshFragment extends Fragment {

    private TextView out;
    private EditText input;
    private EditText user;
    private EditText pass;
    private ScrollView scroll;
    private View creds;
    private View header;
    private View quick;
    private boolean full = false;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Session session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ssh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        out = view.findViewById(R.id.terminal_output);
        input = view.findViewById(R.id.command_input);
        scroll = view.findViewById(R.id.terminal_scroll);
        user = view.findViewById(R.id.ssh_user_input);
        pass = view.findViewById(R.id.ssh_pass_input);
        creds = view.findViewById(R.id.creds_card);
        header = view.findViewById(R.id.ssh_header);
        quick = view.findViewById(R.id.quick_commands_scroll);
        
        MaterialButton btnSend = view.findViewById(R.id.btn_send);
        MaterialButton btnConn = view.findViewById(R.id.btn_connect_ssh);
        MaterialButton btnToggle = view.findViewById(R.id.btn_toggle_creds);
        MaterialButton btnFull = view.findViewById(R.id.btn_fullscreen);

        SecurePrefs prefs = new SecurePrefs(requireContext());
        ServerProfile profile = prefs.getCurrentProfile();
        if (profile != null) {
            user.setText(profile.getSshUser());
            pass.setText(profile.getSshPassword());
        }

        input.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_SEND) {
                run();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> run());
        
        btnConn.setOnClickListener(v -> {
            log(getString(R.string.ssh_connecting_new));
            exec.execute(() -> {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                execute("whoami", null);
            });
            creds.setVisibility(View.GONE);
        });

        btnToggle.setOnClickListener(v -> {
            boolean visible = creds.getVisibility() == View.VISIBLE;
            creds.setVisibility(visible ? View.GONE : View.VISIBLE);
        });

        btnFull.setOnClickListener(v -> toggleFull(view));

        setupQuick(view);

        log("System ready. Type command to execute on TrueNAS.");
    }

    private void toggleFull(View root) {
        full = !full;
        int state = full ? View.GONE : View.VISIBLE;
        
        header.setVisibility(state);
        quick.setVisibility(state);
        
        View card = root.findViewById(R.id.terminal_card);
        MaterialButton btn = root.findViewById(R.id.btn_fullscreen);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
        
        float d = getResources().getDisplayMetrics().density;
        if (full) {
            btn.setIconResource(R.drawable.ic_fullscreen_exit);
            lp.topMargin = (int) (8 * d);
            lp.bottomMargin = (int) (8 * d);
            if (getActivity() instanceof MainActivity) {
                getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            }
        } else {
            btn.setIconResource(R.drawable.ic_fullscreen);
            lp.topMargin = (int) (16 * d);
            lp.bottomMargin = (int) (12 * d);
            if (getActivity() instanceof MainActivity) {
                getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
            }
        }
        card.setLayoutParams(lp);
    }

    private void setupQuick(View root) {
        LinearLayout layout = root.findViewById(R.id.quick_commands_container);
        String[][] data = {
            {"Pool Status", "zpool status"},
            {"ARC Stats", "arc_summary | head -n 20"},
            {"Active Jobs", "midclt call core.get_jobs | jq"},
            {"Disk Space", "df -h"},
            {"Net Stats", "netstat -i"},
            {"Sys Info", "midclt call system.info | jq"},
            {"CPU Load", "top -b -n 1 | head -n 15"}
        };

        int bCol = ContextCompat.getColor(requireContext(), R.color.tg_blue);
        int tCol = ContextCompat.getColor(requireContext(), R.color.white);

        for (String[] info : data) {
            MaterialButton btn = new MaterialButton(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (int) (40 * getResources().getDisplayMetrics().density)
            );
            lp.setMargins(0, 0, (int) (10 * getResources().getDisplayMetrics().density), 0);
            btn.setLayoutParams(lp);
            btn.setText(info[0]);
            btn.setTextSize(12);
            btn.setAllCaps(false);
            btn.setCornerRadius((int) (20 * getResources().getDisplayMetrics().density));
            
            btn.setBackgroundTintList(ColorStateList.valueOf(bCol));
            btn.setTextColor(tCol);
            btn.setAlpha(1.0f);
            btn.setElevation(4 * getResources().getDisplayMetrics().density);

            btn.setOnClickListener(v -> {
                btn.setEnabled(false);
                btn.setAlpha(0.6f);
                exec.execute(() -> execute(info[1], res -> {
                    handler.post(() -> {
                        btn.setEnabled(true);
                        btn.setAlpha(1.0f);
                        CommandResultBottomSheet.newInstance(info[0], res)
                            .show(getChildFragmentManager(), "cmd_result");
                    });
                }));
            });
            layout.addView(btn);
        }
    }

    private void run() {
        String cmd = input.getText().toString().trim();
        if (cmd.isEmpty()) return;

        log("\n$ " + cmd);
        input.setText("");

        exec.execute(() -> execute(cmd, null));
    }

    private void execute(String cmd, @Nullable Callback cb) {
        SecurePrefs prefs = new SecurePrefs(requireContext());
        ServerProfile profile = prefs.getCurrentProfile();

        if (profile == null || profile.getHost().isEmpty()) {
            String err = "Error: No server profile configured.";
            if (cb != null) cb.onResult(err);
            else log("\n" + err);
            return;
        }

        String raw = profile.getHost();
        if ("demo_mode".equals(raw)) {
            String err = getString(R.string.ssh_demo_error);
            if (cb != null) cb.onResult(err);
            else log("\n" + err);
            return;
        }
        
        String clean = raw;
        if (clean.startsWith("http://")) clean = clean.substring(7);
        else if (clean.startsWith("https://")) clean = clean.substring(8);
        if (clean.contains("/")) clean = clean.substring(0, clean.indexOf("/"));
        if (clean.contains(":")) clean = clean.substring(0, clean.indexOf(":"));
        
        final String host = clean;
        StringBuilder res = new StringBuilder();

        try {
            if (session == null || !session.isConnected()) {
                String u = user.getText().toString();
                String p = pass.getText().toString();
                
                handler.post(() -> {
                    if (cb == null) log("\nConnecting to " + host + ":" + profile.getSshPort() + " as " + u + "...");
                });
                
                JSch jsch = new JSch();
                session = jsch.getSession(u, host, profile.getSshPort());
                session.setPassword(p);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("PreferredAuthentications", "password,keyboard-interactive");
                session.connect(15000);
                handler.post(() -> {
                    if (cb == null) log(getString(R.string.ssh_connected));
                });
            }

            ChannelExec chan = (ChannelExec) session.openChannel("exec");
            chan.setCommand(cmd);
            chan.setInputStream(null);
            chan.setErrStream(System.err);
            InputStream is = chan.getInputStream();
            InputStream es = chan.getExtInputStream();
            chan.connect();

            byte[] buf = new byte[1024];
            while (true) {
                while (is.available() > 0) {
                    int read = is.read(buf, 0, 1024);
                    if (read < 0) break;
                    String s = new String(buf, 0, read);
                    if (cb != null) res.append(s);
                    else log(s);
                }
                while (es.available() > 0) {
                    int read = es.read(buf, 0, 1024);
                    if (read < 0) break;
                    String s = new String(buf, 0, read);
                    if (cb != null) res.append("\nERR: ").append(s);
                    else log("\nERR: " + s);
                }
                if (chan.isClosed()) {
                    if (is.available() > 0) continue;
                    break;
                }
                try { Thread.sleep(100); } catch (Exception e) {}
            }
            chan.disconnect();

            if (cb != null) {
                handler.post(() -> cb.onResult(res.toString()));
            }

        } catch (Exception e) {
            Log.e("SSH", "Error connecting or executing", e);
            String err = e.getMessage();
            if (err == null) err = e.toString();
            String fErr = err;
            handler.post(() -> {
                if (cb != null) cb.onResult("Error: " + fErr);
                else log("\nError: " + fErr);
            });
            if (session != null) session.disconnect();
            session = null;
        }
    }

    interface Callback {
        void onResult(String output);
    }

    private void log(String s) {
        handler.post(() -> {
            out.append(s);
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (session != null && session.isConnected()) {
            exec.execute(() -> session.disconnect());
        }
        exec.shutdown();
    }
}
