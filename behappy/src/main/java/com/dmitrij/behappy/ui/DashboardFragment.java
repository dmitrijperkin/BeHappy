package com.dmitrij.behappy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.StatsManager;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.AlertInfo;
import com.dmitrij.behappy.model.SystemInfo;
import com.dmitrij.behappy.model.UsageInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private TextView cpuTxt, ramTxt, poolsTxt, uptimeTxt, versionTxt, statusTxt, cpuModelTxt, ramTotalTxt;
    private ProgressBar cpuProgress, ramProgress;
    private SwipeRefreshLayout refresh;
    private AlertAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            refresh(true);
            handler.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        repo = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());

        uptimeTxt = view.findViewById(R.id.text_uptime);
        versionTxt = view.findViewById(R.id.text_version);
        poolsTxt = view.findViewById(R.id.text_pools);
        cpuTxt = view.findViewById(R.id.text_cpu);
        ramTxt = view.findViewById(R.id.text_ram);
        statusTxt = view.findViewById(R.id.text_status);
        cpuProgress = view.findViewById(R.id.progress_cpu);
        ramProgress = view.findViewById(R.id.progress_ram);
        refresh = view.findViewById(R.id.swipe_refresh);
        cpuModelTxt = view.findViewById(R.id.text_cpu_model);
        ramTotalTxt = view.findViewById(R.id.text_ram_total);

        view.findViewById(R.id.card_cpu).setOnClickListener(v -> openStats(StatsDetailActivity.TYPE_CPU));
        view.findViewById(R.id.card_ram).setOnClickListener(v -> openStats(StatsDetailActivity.TYPE_RAM));

        RecyclerView recycler = view.findViewById(R.id.recycler_alerts);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertAdapter();
        recycler.setAdapter(adapter);

        refresh.setOnRefreshListener(() -> refresh(false));
        
        refresh(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(updateTask, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTask);
    }

    private void openStats(String type) {
        Intent intent = new Intent(requireContext(), StatsDetailActivity.class);
        intent.putExtra(StatsDetailActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    private void refresh(boolean silent) {
        if (prefs.getHost().isEmpty() || prefs.getApiKey().isEmpty()) {
            statusTxt.setVisibility(View.VISIBLE);
            statusTxt.setText(R.string.error_missing_settings);
            refresh.setRefreshing(false);
            return;
        }

        statusTxt.setVisibility(View.GONE);
        if (!silent) refresh.setRefreshing(true);

        fetchStatus(silent);
        fetchUsage();
        fetchAlerts(silent);
        fetchPools();
        fetchNetwork();
    }

    private void fetchNetwork() {
        repo.fetchNetworkUsage(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), net -> {}, err -> {});
    }

    private void fetchStatus(boolean silent) {
        repo.fetchSystemInfo(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.SystemInfoCallback() {
            @Override
            public void onSuccess(SystemInfo info) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        uptimeTxt.setText(formatUptime(info.getUptime()));
                        versionTxt.setText(info.getVersion());
                        if (!silent) refresh.setRefreshing(false);
                    });
                }
            }
            @Override public void onError(String err) {
                if (isAdded() && !silent) {
                    requireActivity().runOnUiThread(() -> refresh.setRefreshing(false));
                }
            }
        });
    }

    private String formatUptime(String raw) {
        if (raw == null || raw.equals("unknown")) return raw;
        try {
            String clean = raw.split("\\.")[0];
            String d = "";
            String t = clean;

            if (clean.contains("day")) {
                String[] p = clean.split(",");
                d = p[0].replace("days", "д").replace("day", "д").trim() + " ";
                if (p.length > 1) t = p[1].trim();
                else return d.trim();
            }

            String[] c = t.split(":");
            if (c.length == 3) {
                int h = Integer.parseInt(c[0]);
                int m = Integer.parseInt(c[1]);
                int s = Integer.parseInt(c[2]);
                
                StringBuilder sb = new StringBuilder(d);
                if (h > 0) sb.append(h).append("ч ");
                if (m > 0 || h > 0) sb.append(m).append("м ");
                sb.append(s).append("с");
                return sb.toString().trim();
            }
            return clean;
        } catch (Exception e) {
            return raw;
        }
    }

    private void fetchUsage() {
        repo.fetchUsage(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.UsageCallback() {
            @Override
            public void onSuccess(UsageInfo info) {
                if (isAdded()) {
                    StatsManager.getInstance().addCpuSample((float) info.getCpuPercent());
                    StatsManager.getInstance().addRamSample((float) info.getRamPercent());

                    requireActivity().runOnUiThread(() -> {
                        cpuTxt.setText(String.format(Locale.getDefault(), "%.1f%%", info.getCpuPercent()));
                        cpuProgress.setProgress((int) info.getCpuPercent());
                        ramTxt.setText(String.format(Locale.getDefault(), "%.1f%%", info.getRamPercent()));
                        ramProgress.setProgress((int) info.getRamPercent());

                        if (info.getCpuModel() != null) cpuModelTxt.setText(info.getCpuModel());
                        if (info.getTotalMemory() > 0) ramTotalTxt.setText(formatSize(info.getTotalMemory()));
                    });
                }
            }
            @Override public void onError(String err) {}
        });
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int i = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, i), units[i]);
    }

    private void fetchAlerts(boolean silent) {
        repo.fetchAlerts(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.AlertsCallback() {
            @Override
            public void onSuccess(List<AlertInfo> list) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        adapter.setAlerts(list);
                        if (!silent) refresh.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String err) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!silent) refresh.setRefreshing(false);
                        statusTxt.setVisibility(View.VISIBLE);
                        statusTxt.setText(err);
                    });
                }
            }
        });
    }

    private void fetchPools() {
        repo.fetchPools(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.PoolsCallback() {
            @Override
            public void onSuccess(List<com.dmitrij.behappy.model.PoolInfo> list) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> poolsTxt.setText(getString(R.string.status_pools, list == null ? 0 : list.size())));
                }
            }
            @Override public void onError(String err) {}
        });
    }
}
