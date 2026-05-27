package com.dmitrij.behappy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.DatasetInfo;
import com.dmitrij.behappy.model.PoolInfo;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PoolAdapter extends RecyclerView.Adapter<PoolAdapter.Holder> {
    private List<PoolInfo> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onPoolClick(PoolInfo info);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setPools(List<PoolInfo> list) {
        final List<PoolInfo> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getName(), newList.get(newPos).getName());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos), newList.get(newPos));
            }
        });
        items = new ArrayList<>(newList);
        res.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pool, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        PoolInfo info = items.get(pos);
        holder.name.setText(info.getName());
        
        String health = info.getHealthLabel();
        String local = health;
        if ("HEALTHY".equalsIgnoreCase(health)) local = holder.itemView.getContext().getString(R.string.status_healthy);
        else if ("ONLINE".equalsIgnoreCase(health)) local = holder.itemView.getContext().getString(R.string.status_online);
        else if ("OFFLINE".equalsIgnoreCase(health)) local = holder.itemView.getContext().getString(R.string.status_offline);
        
        holder.status.setText(local);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPoolClick(info);
        });

        if ("HEALTHY".equalsIgnoreCase(info.getHealthLabel()) || "ONLINE".equalsIgnoreCase(info.getHealthLabel())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("DEGRADED".equalsIgnoreCase(info.getHealthLabel()) || "OFFLINE".equalsIgnoreCase(info.getHealthLabel())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiary));
        }

        holder.space.setText(holder.itemView.getContext().getString(R.string.label_free_format, formatSize(info.getFree()), formatSize(info.getSize())));

        List<DatasetInfo> datasets = info.getDatasets();
        if (datasets != null && !datasets.isEmpty()) {
            StringBuilder sb = new StringBuilder(holder.itemView.getContext().getString(R.string.label_datasets_list));
            for (int i = 0; i < datasets.size(); i++) {
                DatasetInfo ds = datasets.get(i);
                sb.append("• ").append(ds.getName());
                
                if (ds.getUsed() != null && ds.getAvailable() != null) {
                    String u = ds.getUsed().getValue() != null ? ds.getUsed().getValue() : formatSize(ds.getUsed().getRaw());
                    String a = ds.getAvailable().getValue() != null ? ds.getAvailable().getValue() : formatSize(ds.getAvailable().getRaw());
                    sb.append(" ").append(holder.itemView.getContext().getString(R.string.label_used_avail, u, a));
                }
                
                if (i < datasets.size() - 1) sb.append("\n");
            }
            holder.datasets.setText(sb.toString());
            holder.datasets.setVisibility(View.VISIBLE);
        } else {
            holder.datasets.setVisibility(View.GONE);
        }
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int i = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, i), units[i]);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, status, space, datasets;

        public Holder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.pool_name);
            status = view.findViewById(R.id.pool_status);
            space = view.findViewById(R.id.pool_space);
            datasets = view.findViewById(R.id.pool_datasets);
        }
    }
}
