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
import com.dmitrij.behappy.model.ServiceInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.List;

public class ServicesFragment extends Fragment {
    private TrueNasRepository repository;
    private SecurePrefs prefs;
    private ServiceAdapter adapter;
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
        adapter = new ServiceAdapter();
        adapter.setOnServiceActionListener(this::handleServiceAction);
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadServices);
        loadServices();
    }

    private void loadServices() {
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        repository.fetchServices(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.ServicesCallback() {
            @Override
            public void onSuccess(List<ServiceInfo> info) {
                if (isAdded() && getView() != null) {
                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    if (adapter != null) adapter.setServices(info);
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

    private void handleServiceAction(ServiceInfo service) {
        boolean isRunning = "RUNNING".equalsIgnoreCase(service.getState());
        TrueNasRepository.ActionCallback callback = new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded() && getView() != null) {
                    Toast.makeText(getContext(), messageResId, Toast.LENGTH_SHORT).show();
                    loadServices();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded() && getView() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        };

        if (isRunning) {
            repository.stopService(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), service, callback);
        } else {
            repository.startService(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), service, callback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadingProgress = null;
        swipeRefresh = null;
        emptyText = null;
        adapter = null;
    }
}
