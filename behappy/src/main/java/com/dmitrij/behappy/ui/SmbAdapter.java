package com.dmitrij.behappy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.SmbShare;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SmbAdapter extends RecyclerView.Adapter<SmbAdapter.SmbViewHolder> {
    private List<SmbShare> shares = new ArrayList<>();
    private OnSmbToggleListener listener;

    public interface OnSmbToggleListener {
        void onToggle(SmbShare share, boolean isEnabled);
    }

    public void setShares(List<SmbShare> newShares) {
        final List<SmbShare> latest = newShares != null ? newShares : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return shares.size(); }
            @Override public int getNewListSize() { return latest.size(); }
            @Override public boolean areItemsTheSame(int oldP, int newP) {
                return shares.get(oldP).getId() == latest.get(newP).getId();
            }
            @Override public boolean areContentsTheSame(int oldP, int newP) {
                return Objects.equals(shares.get(oldP), latest.get(newP));
            }
        });
        this.shares = new ArrayList<>(latest);
        result.dispatchUpdatesTo(this);
    }

    public void setOnSmbToggleListener(OnSmbToggleListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SmbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smb, parent, false);
        return new SmbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmbViewHolder holder, int position) {
        SmbShare share = shares.get(position);
        holder.name.setText(share.getName());
        holder.path.setText(share.getPath());
        
        holder.toggle.setOnCheckedChangeListener(null);
        holder.toggle.setChecked(share.isEnabled());
        holder.toggle.setOnCheckedChangeListener((v, isChecked) -> {
            if (listener != null) listener.onToggle(share, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return shares.size();
    }

    static class SmbViewHolder extends RecyclerView.ViewHolder {
        TextView name, path;
        MaterialSwitch toggle;

        public SmbViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.smb_name);
            path = itemView.findViewById(R.id.smb_path);
            toggle = itemView.findViewById(R.id.smb_switch);
        }
    }
}
