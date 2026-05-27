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
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private VmAdapter adapter;
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
        adapter = new VmAdapter();
        adapter.setActionListener(this::onAction);
        recycler.setAdapter(adapter);
        
        refresh.setOnRefreshListener(this::fetch);
        fetch();
        
        return view;
    }

    private void fetch() {
        progress.setVisibility(View.VISIBLE);
        repo.fetchVms(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.VmsCallback() {
            @Override
            public void onSuccess(List<VmInfo> list) {
                update(new ArrayList<>(list));
            }

            @Override
            public void onError(String err) {
                update(new ArrayList<>());
                if (isAdded()) {
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void update(final List<? extends VmAdapter.Displayable> list) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
                refresh.setRefreshing(false);
                adapter.setItems(new ArrayList<>(list));
                empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void onAction(VmAdapter.Displayable item) {
        if (item instanceof VmInfo vm) {
            processAction(vm);
        }
    }

    private TrueNasRepository.ActionCallback createCallback() {
        return new TrueNasRepository.ActionCallback() {
            @Override
            public void onDone(int resId) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
                        fetch();
                    });
                }
            }

            @Override
            public void onError(String err) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show());
                }
            }
        };
    }

    private void processAction(VmInfo vm) {
        if (vm.isRunning()) {
            repo.stopVm(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), vm.getId(), createCallback());
        } else {
            repo.startVm(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), vm.getId(), createCallback());
        }
    }
}
