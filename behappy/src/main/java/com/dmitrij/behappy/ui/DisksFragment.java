package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.DiskInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.List;

public class DisksFragment extends Fragment {
    private TrueNasRepository repository;
    private SecurePrefs prefs;
    private DiskAdapter adapter;
    private ProgressBar loadingProgress;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repository = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        loadingProgress = view.findViewById(R.id.loading_progress);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyText = view.findViewById(R.id.text_empty);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiskAdapter();
        adapter.setListener(this::showDiskDetail);
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadDisks);
        loadDisks();
    }

    private void showDiskDetail(DiskInfo disk) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_disk_detail, null);
        
        TextView title = view.findViewById(R.id.detail_title);
        TextView percentage = view.findViewById(R.id.detail_percentage);
        ProgressBar progress = view.findViewById(R.id.detail_progress);
        TextView info = view.findViewById(R.id.detail_info);
        TextView extra = view.findViewById(R.id.detail_extra);

        title.setText(getString(R.string.label_disk_title, disk.getName()));
        percentage.setText(R.string.disk_healthy);
        progress.setProgress(100);
        
        String sizeStr = formatSize(disk.getSize());
        info.setText(getString(R.string.disk_size_format, sizeStr));
        
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.label_model, disk.getModel())).append("\n");
        sb.append(getString(R.string.label_serial, disk.getSerial())).append("\n");
        sb.append(getString(R.string.label_type, disk.getType())).append("\n");
        if (disk.getTemperature() != null) {
            sb.append(getString(R.string.label_temperature, disk.getTemperature()));
        }
        extra.setText(sb.toString());

        dialog.setContentView(view);
        dialog.show();
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void loadDisks() {
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        repository.fetchDisksWithTemps(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.DisksCallback() {
            @Override
            public void onSuccess(List<DiskInfo> info) {
                if (isAdded() && getView() != null) {
                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    if (adapter != null) adapter.setDisks(info);
                    if (emptyText != null) {
                        emptyText.setVisibility(info == null || info.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded() && getView() != null) {
                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
