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
import com.dmitrij.behappy.model.IxAppInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment {
    private TrueNasRepository repository;
    private SecurePrefs prefs;
    private VmAdapter adapter;
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
        adapter = new VmAdapter();
        adapter.setOnItemActionListener(this::handleAction);
        adapter.setOnItemUpdateListener(this::handleUpdate);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadApps);
        loadApps();
    }

    private void handleUpdate(VmAdapter.Displayable item) {
        if (!(item instanceof IxAppInfo)) return;
        IxAppInfo app = (IxAppInfo) item;
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.btn_update)
                .setMessage(getString(R.string.msg_update_found, app.getVersion()) + "?")
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    loadingProgress.setVisibility(View.VISIBLE);
                    repository.upgradeApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), new TrueNasRepository.ActionCallback() {
                        @Override
                        public void onDone(int messageResId) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                loadingProgress.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show();
                                loadApps();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                loadingProgress.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void loadApps() {
        loadingProgress.setVisibility(View.VISIBLE);
        repository.fetchApps(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.AppsCallback() {
            @Override
            public void onSuccess(List<IxAppInfo> info) {
                updateUI(info == null ? new ArrayList<>() : new ArrayList<>(info));
            }

            @Override
            public void onError(String message) {
                updateUI(new ArrayList<>());
                if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(final List<VmAdapter.Displayable> items) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            loadingProgress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            adapter.setItems(items);
            emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void handleAction(VmAdapter.Displayable item) {
        if (!(item instanceof IxAppInfo)) return;
        IxAppInfo app = (IxAppInfo) item;
        TrueNasRepository.ActionCallback callback = new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
                    loadApps();
                });
            }

            @Override
            public void onError(String message) {
                if (isAdded()) requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
            }
        };

        if (app.isRunning()) {
            repository.stopApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), callback);
        } else {
            repository.startApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), callback);
        }
    }
}
