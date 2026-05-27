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
import com.dmitrij.behappy.model.PoolInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.List;

public class PoolsFragment extends Fragment {
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private PoolAdapter adapter;
    private ProgressBar progress;
    private SwipeRefreshLayout refresh;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        
        repo = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());
        
        RecyclerView recycler = view.findViewById(R.id.recycler_view);
        progress = view.findViewById(R.id.loading_progress);
        refresh = view.findViewById(R.id.swipe_refresh);
        empty = view.findViewById(R.id.text_empty);
        
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PoolAdapter();
        adapter.setListener(this::onDetail);
        recycler.setAdapter(adapter);
        
        refresh.setOnRefreshListener(this::fetch);
        fetch();
        
        return view;
    }

    private void onDetail(PoolInfo pool) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_disk_detail, null);
        
        TextView title = view.findViewById(R.id.detail_title);
        TextView pctTxt = view.findViewById(R.id.detail_percentage);
        ProgressBar bar = view.findViewById(R.id.detail_progress);
        TextView infoTxt = view.findViewById(R.id.detail_info);
        TextView extra = view.findViewById(R.id.detail_extra);

        title.setText(getString(R.string.label_pool_title, pool.getName()));
        
        long used = pool.getSize() - pool.getFree();
        double pct = pool.getSize() <= 0 ? 0 : (double) used / pool.getSize() * 100.0;

        pctTxt.setText(getString(R.string.pool_used_pct, pct));
        bar.setProgress((int) pct);
        
        infoTxt.setText(getString(R.string.pool_usage_format, formatSize(used), formatSize(pool.getSize())));
        
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.label_status_with_colon, pool.getHealthLabel())).append("\n");
        sb.append(getString(R.string.label_free, formatSize(pool.getFree()))).append("\n");
        
        if (pool.getDatasets() != null && !pool.getDatasets().isEmpty()) {
            sb.append(getString(R.string.header_datasets));
            for (com.dmitrij.behappy.model.DatasetInfo ds : pool.getDatasets()) {
                sb.append("• ").append(ds.getName()).append("\n");
            }
        }
        extra.setText(sb.toString());

        com.google.android.material.button.MaterialButton btn = new com.google.android.material.button.MaterialButton(requireContext());
        btn.setText(R.string.btn_start_scrub);
        btn.setOnClickListener(v -> {
            if (pool.getId() == null) {
                Toast.makeText(requireContext(), R.string.err_pool_id_missing, Toast.LENGTH_SHORT).show();
                return;
            }
            repo.scrubPool(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), pool.getId(), new TrueNasRepository.ActionCallback() {
                @Override
                public void onDone(int resId) {
                    Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                @Override
                public void onError(String err) {
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
                }
            });
        });

        ((ViewGroup) view).addView(btn);
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
        progress.setVisibility(View.VISIBLE);
        repo.fetchPoolsWithDatasets(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.PoolsCallback() {
            @Override
            public void onSuccess(List<PoolInfo> list) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        refresh.setRefreshing(false);
                        adapter.setPools(list);
                        empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                }
            }

            @Override
            public void onError(String err) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        refresh.setRefreshing(false);
                        empty.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
