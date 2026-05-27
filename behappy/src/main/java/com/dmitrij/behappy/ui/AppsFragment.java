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
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private VmAdapter adapter;
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
        adapter = new VmAdapter();
        adapter.setActionListener(this::onAction);
        adapter.setUpdateListener(this::onUpdate);
        recycler.setAdapter(adapter);

        refresh.setOnRefreshListener(this::fetch);
        fetch();
    }

    private void onUpdate(VmAdapter.Displayable item) {
        if (!(item instanceof IxAppInfo app)) return;
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.btn_update)
                .setMessage(getString(R.string.msg_update_found, app.getVersion()) + "?")
                .setPositiveButton(R.string.dialog_confirm, (di, i) -> {
                    progress.setVisibility(View.VISIBLE);
                    repo.upgradeApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), new TrueNasRepository.ActionCallback() {
                        @Override
                        public void onDone(int resId) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), resId, Toast.LENGTH_LONG).show();
                                fetch();
                            });
                        }

                        @Override
                        public void onError(String err) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void fetch() {
        progress.setVisibility(View.VISIBLE);
        repo.fetchApps(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.AppsCallback() {
            @Override
            public void onSuccess(List<IxAppInfo> list) {
                update(list == null ? new ArrayList<>() : new ArrayList<>(list));
            }

            @Override
            public void onError(String err) {
                update(new ArrayList<>());
                if (isAdded()) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void update(final List<VmAdapter.Displayable> list) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            refresh.setRefreshing(false);
            adapter.setItems(list);
            empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void onAction(VmAdapter.Displayable item) {
        if (!(item instanceof IxAppInfo app)) return;
        TrueNasRepository.ActionCallback cb = new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int resId) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
                    fetch();
                });
            }

            @Override
            public void onError(String err) {
                if (isAdded()) requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show());
            }
        };

        if (app.isRunning()) {
            repo.stopApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), cb);
        } else {
            repo.startApp(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), app.getName(), cb);
        }
    }
}
