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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.security.SecurePrefs;

public class LogsFragment extends Fragment {
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private TextView logsTxt;
    private ProgressBar progress;
    private SwipeRefreshLayout refresh;
    private androidx.appcompat.widget.SearchView search;
    private String full = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());

        logsTxt = view.findViewById(R.id.text_logs);
        progress = view.findViewById(R.id.loading_progress);
        refresh = view.findViewById(R.id.swipe_refresh);
        search = view.findViewById(R.id.search_logs);

        search.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                filter(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                filter(q);
                return true;
            }
        });

        refresh.setOnRefreshListener(this::fetch);
        fetch();
    }

    private void filter(String q) {
        if (q == null || q.isEmpty()) {
            logsTxt.setText(full);
            return;
        }

        String[] lines = full.split("\n");
        StringBuilder sb = new StringBuilder();
        String low = q.toLowerCase();
        
        for (String line : lines) {
            if (line.toLowerCase().contains(low)) {
                sb.append(line).append("\n");
            }
        }
        
        if (sb.length() == 0) {
            logsTxt.setText("No matches found for: " + q);
        } else {
            logsTxt.setText(sb.toString());
        }
    }

    private void fetch() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        repo.fetchLogs(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.StringCallback() {
            @Override
            public void onSuccess(String data) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    full = data;
                    filter(search.getQuery().toString());
                }
            }

            @Override
            public void onError(String err) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
