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
    private TrueNasRepository repository;
    private SecurePrefs prefs;
    private TextView cpuText, ramText, poolsText, uptimeText, versionText, statusText, cpuModelText, ramTotalText;
    private ProgressBar cpuProgress, ramProgress;
    private SwipeRefreshLayout swipeRefresh;
    private AlertAdapter alertAdapter;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData(true);
            refreshHandler.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        repository = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());

        uptimeText = view.findViewById(R.id.text_uptime);
        versionText = view.findViewById(R.id.text_version);
        poolsText = view.findViewById(R.id.text_pools);
        cpuText = view.findViewById(R.id.text_cpu);
        ramText = view.findViewById(R.id.text_ram);
        statusText = view.findViewById(R.id.text_status);
        cpuProgress = view.findViewById(R.id.progress_cpu);
        ramProgress = view.findViewById(R.id.progress_ram);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        cpuModelText = view.findViewById(R.id.text_cpu_model);
        ramTotalText = view.findViewById(R.id.text_ram_total);

        View cpuCard = view.findViewById(R.id.card_cpu);
        View ramCard = view.findViewById(R.id.card_ram);

        cpuCard.setOnClickListener(v -> openStats(StatsDetailActivity.TYPE_CPU));
        ramCard.setOnClickListener(v -> openStats(StatsDetailActivity.TYPE_RAM));

        RecyclerView alertRecycler = view.findViewById(R.id.recycler_alerts);
        alertRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        alertAdapter = new AlertAdapter();
        alertRecycler.setAdapter(alertAdapter);

        swipeRefresh.setOnRefreshListener(() -> refreshData(false));
        
        refreshData(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void openStats(String type) {
        Intent intent = new Intent(requireContext(), StatsDetailActivity.class);
        intent.putExtra(StatsDetailActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    private void refreshData(boolean silent) {
        if (prefs.getHost().isEmpty() || prefs.getApiKey().isEmpty()) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.error_missing_settings);
            swipeRefresh.setRefreshing(false);
            return;
        }

        statusText.setVisibility(View.GONE);
        if (!silent) swipeRefresh.setRefreshing(true);

        loadSystemInfo(silent);
        loadUsage();
        loadAlerts(silent);
        loadPoolsCount();
        loadNetworkUsage();
    }

    private void loadNetworkUsage() {
        repository.fetchNetworkUsage(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), data -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                });
            }
        }, msg -> {});
    }

    private void loadSystemInfo(boolean silent) {
        repository.fetchSystemInfo(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.SystemInfoCallback() {
            @Override
            public void onSuccess(SystemInfo i) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        uptimeText.setText(formatUptime(i.getUptime()));
                        versionText.setText(i.getVersion());
                        if (!silent) swipeRefresh.setRefreshing(false);
                    });
                }
            }
            @Override public void onError(String m) {
                if (isAdded() && !silent) {
                    requireActivity().runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                }
            }
        });
    }

    private String formatUptime(String raw) {
        if (raw == null || raw.equals("unknown")) return raw;
        try {
            String clean = raw.split("\\.")[0];
            String days = "";
            String timePart = clean;

            if (clean.contains("day")) {
                String[] parts = clean.split(",");
                days = parts[0].replace("days", "д").replace("day", "д").trim() + " ";
                if (parts.length > 1) {
                    timePart = parts[1].trim();
                } else {
                    return days.trim();
                }
            }

            String[] time = timePart.split(":");
            if (time.length == 3) {
                int h = Integer.parseInt(time[0]);
                int m = Integer.parseInt(time[1]);
                int s = Integer.parseInt(time[2]);
                
                StringBuilder sb = new StringBuilder(days);
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

    private void loadUsage() {
        repository.fetchUsage(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.UsageCallback() {
            @Override
            public void onSuccess(UsageInfo i) {
                if (isAdded()) {
                    StatsManager.getInstance().addCpuSample((float) i.getCpuPercent());
                    StatsManager.getInstance().addRamSample((float) i.getRamPercent());

                    requireActivity().runOnUiThread(() -> {
                        cpuText.setText(String.format(Locale.getDefault(), "%.1f%%", i.getCpuPercent()));
                        cpuProgress.setProgress((int) i.getCpuPercent());
                        ramText.setText(String.format(Locale.getDefault(), "%.1f%%", i.getRamPercent()));
                        ramProgress.setProgress((int) i.getRamPercent());

                        if (i.getCpuModel() != null) cpuModelText.setText(i.getCpuModel());
                        if (i.getTotalMemory() > 0) ramTotalText.setText(formatSize(i.getTotalMemory()));
                    });
                }
            }
            @Override public void onError(String m) {}
        });
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void loadAlerts(boolean silent) {
        repository.fetchAlerts(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.AlertsCallback() {
            @Override
            public void onSuccess(List<AlertInfo> alerts) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        alertAdapter.setAlerts(alerts);
                        if (!silent) swipeRefresh.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String m) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!silent) swipeRefresh.setRefreshing(false);
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(m);
                    });
                }
            }
        });
    }

    private void loadPoolsCount() {
        repository.fetchPools(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.PoolsCallback() {
            @Override
            public void onSuccess(java.util.List<com.dmitrij.behappy.model.PoolInfo> info) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> poolsText.setText(getString(R.string.status_pools, info == null ? 0 : info.size())));
                }
            }
            @Override public void onError(String m) {}
        });
    }
}
