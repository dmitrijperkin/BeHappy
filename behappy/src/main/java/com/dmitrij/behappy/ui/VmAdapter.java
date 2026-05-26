package com.dmitrij.behappy.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VmAdapter extends RecyclerView.Adapter<VmAdapter.VmViewHolder> {
    
    public interface Displayable {
        String getDisplayName();
        String getDisplayStatus();
        String getDisplayInfo();
        boolean isRunning();
        default boolean hasUpdate() { return false; }
    }

    private List<Displayable> items = new ArrayList<>();
    private OnItemActionListener listener;
    private OnItemUpdateListener updateListener;

    public interface OnItemActionListener {
        void onAction(Displayable item);
    }

    public interface OnItemUpdateListener {
        void onUpdate(Displayable item);
    }

    public void setItems(List<Displayable> newItems) {
        final List<Displayable> latestItems = newItems != null ? newItems : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return latestItems.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(items.get(oldItemPosition).getDisplayName(), latestItems.get(newItemPosition).getDisplayName());
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Displayable oldI = items.get(oldItemPosition);
                Displayable newI = latestItems.get(newItemPosition);
                return Objects.equals(oldI.getDisplayStatus(), newI.getDisplayStatus()) &&
                       Objects.equals(oldI.getDisplayInfo(), newI.getDisplayInfo()) &&
                       oldI.isRunning() == newI.isRunning() &&
                       oldI.hasUpdate() == newI.hasUpdate();
            }
        });
        this.items = new ArrayList<>(latestItems);
        result.dispatchUpdatesTo(this);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setOnItemUpdateListener(OnItemUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public VmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vm, parent, false);
        return new VmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VmViewHolder holder, int position) {
        Displayable item = items.get(position);
        holder.name.setText(item.getDisplayName());
        
        String status = item.getDisplayStatus();
        String translatedStatus = status;
        if ("RUNNING".equalsIgnoreCase(status)) translatedStatus = holder.itemView.getContext().getString(R.string.status_running);
        else if ("STOPPED".equalsIgnoreCase(status)) translatedStatus = holder.itemView.getContext().getString(R.string.status_stopped);
        else if ("OFFLINE".equalsIgnoreCase(status)) translatedStatus = holder.itemView.getContext().getString(R.string.status_offline);
        else if ("ONLINE".equalsIgnoreCase(status)) translatedStatus = holder.itemView.getContext().getString(R.string.status_online);

        holder.info.setText(String.format("%s • %s", translatedStatus, item.getDisplayInfo()));
        
        holder.actionBtn.setText(item.isRunning() ? R.string.action_stop : R.string.action_start);
        
        if (item.isRunning()) {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("STOPPED".equalsIgnoreCase(item.getDisplayStatus()) || "OFFLINE".equalsIgnoreCase(item.getDisplayStatus())) {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline));
        }

        if (item.hasUpdate()) {
            holder.updateBadge.setVisibility(View.VISIBLE);
            holder.updateBtn.setVisibility(View.VISIBLE);
        } else {
            holder.updateBadge.setVisibility(View.GONE);
            holder.updateBtn.setVisibility(View.GONE);
        }
        
        holder.actionBtn.setOnClickListener(v -> {
            if (listener != null) listener.onAction(item);
        });

        holder.updateBtn.setOnClickListener(v -> {
            if (updateListener != null) updateListener.onUpdate(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VmViewHolder extends RecyclerView.ViewHolder {
        TextView name, info, updateBadge;
        Button actionBtn, updateBtn;

        public VmViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entity_name);
            info = itemView.findViewById(R.id.entity_info);
            updateBadge = itemView.findViewById(R.id.update_badge);
            actionBtn = itemView.findViewById(R.id.btn_action);
            updateBtn = itemView.findViewById(R.id.btn_update);
        }
    }
}
