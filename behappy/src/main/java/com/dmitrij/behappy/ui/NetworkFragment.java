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
import com.dmitrij.behappy.model.NetworkInterface;
import com.dmitrij.behappy.security.SecurePrefs;

import java.util.List;

public class NetworkFragment extends Fragment {
    private TrueNasRepository repo;
    private SecurePrefs prefs;
    private NetworkAdapter adapter;
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
        adapter = new NetworkAdapter();
        recycler.setAdapter(adapter);

        adapter.setListener((name, en) -> {
            int resId = en ? R.string.msg_interface_enabled : R.string.msg_interface_disabled;
            Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
        });

        refresh.setOnRefreshListener(this::fetch);
        fetch();
    }

    private void fetch() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        repo.fetchNetworkInterfaces(requireContext(), prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.NetworkCallback() {
            @Override
            public void onSuccess(List<NetworkInterface> list) {
                if (isAdded() && getView() != null) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (refresh != null) refresh.setRefreshing(false);
                    adapter.setInterfaces(list);
                    empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
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
