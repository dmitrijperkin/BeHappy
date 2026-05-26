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
import com.dmitrij.behappy.model.VmInfo;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.ArrayList;
import java.util.List;

public class VmFragment extends Fragment {
    private TrueNasRepository repository;
    private SecurePrefs prefs;
    private VmAdapter adapter;
    private ProgressBar loadingProgress;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        
        repository = TrueNasRepository.getInstance();
        prefs = new SecurePrefs(requireContext());
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        loadingProgress = view.findViewById(R.id.loading_progress);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyText = view.findViewById(R.id.text_empty);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VmAdapter();
        adapter.setOnItemActionListener(this::handleAction);
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
        
        return view;
    }

    private void loadData() {
        loadingProgress.setVisibility(View.VISIBLE);
        
        repository.fetchVms(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.VmsCallback() {
            @Override
            public void onSuccess(List<VmInfo> vms) {
                updateUI(new ArrayList<>(vms));
            }

            @Override
            public void onError(String message) {
                updateUI(new ArrayList<>());
                if (isAdded()) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateUI(final List<? extends VmAdapter.Displayable> list) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                loadingProgress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                adapter.setItems(new ArrayList<>(list));
                emptyText.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void handleAction(VmAdapter.Displayable item) {
        if (item instanceof VmInfo) {
            handleVmAction((VmInfo) item);
        }
    }

    private TrueNasRepository.ActionCallback getActionCallback() {
        return new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int messageResId) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
                        loadData();
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
                }
            }
        };
    }

    private void handleVmAction(VmInfo vm) {
        if (vm.isRunning()) {
            repository.stopVm(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), vm.getId(), getActionCallback());
        } else {
            repository.startVm(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), vm.getId(), getActionCallback());
        }
    }
}
