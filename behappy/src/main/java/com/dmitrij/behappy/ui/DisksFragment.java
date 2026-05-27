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
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private DiskAdapter adapter;
    private ProgressBar progress;
    private SwipeRefreshLayout refresh;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repo = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());
        
        RecyclerView recycler = view.findViewById(R.id.recycler_view);
        progress = view.findViewById(R.id.loading_progress);
        refresh = view.findViewById(R.id.swipe_refresh);
        empty = view.findViewById(R.id.text_empty);
        
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiskAdapter();
        adapter.setListener(this::onDetail);
        recycler.setAdapter(adapter);
        
        refresh.setOnRefreshListener(this::fetch);
        fetch();
    }

    private void onDetail(DiskInfo disk) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_disk_detail, null);
        
        TextView title = view.findViewById(R.id.detail_title);
        TextView health = view.findViewById(R.id.detail_percentage);
        ProgressBar bar = view.findViewById(R.id.detail_progress);
        TextView info = view.findViewById(R.id.detail_info);
        TextView extra = view.findViewById(R.id.detail_extra);

        title.setText(getString(R.string.label_disk_title, disk.getName()));
        health.setText(R.string.disk_healthy);
        bar.setProgress(100);
        
        String size = formatSize(disk.getSize());
        info.setText(getString(R.string.disk_size_format, size));
        
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
        int i = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024, i), units[i]);
    }

    private void fetch() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        repo.fetchDisksWithTemps(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.DisksCallback() {
            @Override
            public void onSuccess(List<DiskInfo> list) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    if (adapter != null) adapter.setDisks(list);
                    if (empty != null) {
                        empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onError(String err) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    if (empty != null) empty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
