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

public class SmbAdapter extends RecyclerView.Adapter<SmbAdapter.Holder> {
    private List<SmbShare> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onToggle(SmbShare item, boolean en);
    }

    public void setItems(List<SmbShare> list) {
        final List<SmbShare> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).getId() == newList.get(newPos).getId();
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos), newList.get(newPos));
            }
        });
        items = new ArrayList<>(newList);
        res.dispatchUpdatesTo(this);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smb, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        SmbShare item = items.get(pos);
        holder.name.setText(item.getName());
        holder.path.setText(item.getPath());
        
        holder.sw.setOnCheckedChangeListener(null);
        holder.sw.setChecked(item.isEnabled());
        holder.sw.setOnCheckedChangeListener((v, en) -> {
            if (listener != null) listener.onToggle(item, en);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, path;
        MaterialSwitch sw;

        public Holder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.smb_name);
            path = view.findViewById(R.id.smb_path);
            sw = view.findViewById(R.id.smb_switch);
        }
    }
}
