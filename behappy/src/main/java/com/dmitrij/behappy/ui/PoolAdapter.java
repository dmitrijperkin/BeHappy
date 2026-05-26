package com.dmitrij.behappy.ui;

import android.graphics.Color;
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

public class PoolAdapter extends RecyclerView.Adapter<PoolAdapter.PoolViewHolder> {
    private List<PoolInfo> pools = new ArrayList<>();
    private OnPoolClickListener listener;

    public interface OnPoolClickListener {
        void onPoolClick(PoolInfo pool);
    }

    public void setListener(OnPoolClickListener listener) {
        this.listener = listener;
    }

    public void setPools(List<PoolInfo> newPools) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return pools.size(); }
            @Override
            public int getNewListSize() { return newPools.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(pools.get(oldItemPosition).getName(), newPools.get(newItemPosition).getName());
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(pools.get(oldItemPosition), newPools.get(newItemPosition));
            }
        });
        this.pools = new ArrayList<>(newPools);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public PoolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pool, parent, false);
        return new PoolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PoolViewHolder holder, int position) {
        PoolInfo pool = pools.get(position);
        holder.name.setText(pool.getName());
        
        String health = pool.getHealthLabel();
        String translatedHealth = health;
        if ("HEALTHY".equalsIgnoreCase(health)) translatedHealth = holder.itemView.getContext().getString(R.string.status_healthy);
        else if ("ONLINE".equalsIgnoreCase(health)) translatedHealth = holder.itemView.getContext().getString(R.string.status_online);
        else if ("OFFLINE".equalsIgnoreCase(health)) translatedHealth = holder.itemView.getContext().getString(R.string.status_offline);
        
        holder.status.setText(translatedHealth);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPoolClick(pool);
        });

        if ("HEALTHY".equalsIgnoreCase(pool.getHealthLabel()) || "ONLINE".equalsIgnoreCase(pool.getHealthLabel())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("DEGRADED".equalsIgnoreCase(pool.getHealthLabel()) || "OFFLINE".equalsIgnoreCase(pool.getHealthLabel())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiary));
        }

        holder.space.setText(holder.itemView.getContext().getString(R.string.label_free_format, formatSize(pool.getFree()), formatSize(pool.getSize())));

        List<DatasetInfo> datasets = pool.getDatasets();
        if (datasets != null && !datasets.isEmpty()) {
            StringBuilder sb = new StringBuilder(holder.itemView.getContext().getString(R.string.label_datasets_list));
            for (int i = 0; i < datasets.size(); i++) {
                DatasetInfo ds = datasets.get(i);
                sb.append("• ").append(ds.getName());
                
                if (ds.getUsed() != null && ds.getAvailable() != null) {
                    String usedStr = ds.getUsed().getValue() != null ? ds.getUsed().getValue() : formatSize(ds.getUsed().getRaw());
                    String availStr = ds.getAvailable().getValue() != null ? ds.getAvailable().getValue() : formatSize(ds.getAvailable().getRaw());
                    sb.append(" ").append(holder.itemView.getContext().getString(R.string.label_used_avail, usedStr, availStr));
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
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public int getItemCount() {
        return pools.size();
    }

    static class PoolViewHolder extends RecyclerView.ViewHolder {
        TextView name, status, space, datasets;

        public PoolViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pool_name);
            status = itemView.findViewById(R.id.pool_status);
            space = itemView.findViewById(R.id.pool_space);
            datasets = itemView.findViewById(R.id.pool_datasets);
        }
    }
}
