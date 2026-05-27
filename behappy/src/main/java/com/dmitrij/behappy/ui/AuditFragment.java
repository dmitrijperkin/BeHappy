package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.AuditEntry;
import com.dmitrij.behappy.security.SecurePrefs;
import com.dmitrij.behappy.ui.adapter.AuditAdapter;

import java.util.ArrayList;
import java.util.List;

public class AuditFragment extends Fragment {
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private RecyclerView recycler;
    private AuditAdapter adapter;
    private ProgressBar progress;
    private SwipeRefreshLayout refresh;
    private AutoCompleteTextView selector;
    private androidx.appcompat.widget.SearchView search;
    
    private List<AuditEntry> all = new ArrayList<>();
    private final String[] services = {"ALL", "MIDDLEWARE", "SMB", "SUDO", "SYSTEM"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());

        recycler = view.findViewById(R.id.recycler_audit);
        progress = view.findViewById(R.id.loading_progress);
        refresh = view.findViewById(R.id.swipe_refresh);
        selector = view.findViewById(R.id.service_selector);
        search = view.findViewById(R.id.search_audit);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuditAdapter();
        recycler.setAdapter(adapter);

        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, services);
        selector.setAdapter(sAdapter);
        selector.setText(services[0], false);
        selector.setOnItemClickListener((parent, view1, pos, id) -> fetch());

        view.findViewById(R.id.btn_export_csv).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Exporting " + all.size() + " entries...", Toast.LENGTH_SHORT).show()
        );

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
            adapter.setItems(all);
            return;
        }

        List<AuditEntry> res = new ArrayList<>();
        String low = q.toLowerCase();
        
        for (AuditEntry e : all) {
            if (e.getEvent().toLowerCase().contains(low) || 
                e.getUser().toLowerCase().contains(low)) {
                res.add(e);
            }
        }
        adapter.setItems(res);
    }

    private void fetch() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        String s = selector.getText().toString();
        
        repo.fetchAudit(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), s, new TrueNasRepository.AuditCallback() {
            @Override
            public void onSuccess(List<AuditEntry> list) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    all = list;
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
