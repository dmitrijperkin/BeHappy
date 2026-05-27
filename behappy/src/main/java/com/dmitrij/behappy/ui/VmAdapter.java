package com.dmitrij.behappy.ui;

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

public class VmAdapter extends RecyclerView.Adapter<VmAdapter.Holder> {
    
    public interface Displayable {
        String getDisplayName();
        String getDisplayStatus();
        String getDisplayInfo();
        boolean isRunning();
        default boolean hasUpdate() { return false; }
    }

    private List<Displayable> items = new ArrayList<>();
    private ActionListener actionListener;
    private UpdateListener updateListener;

    public interface ActionListener {
        void onAction(Displayable item);
    }

    public interface UpdateListener {
        void onUpdate(Displayable item);
    }

    public void setItems(List<Displayable> list) {
        final List<Displayable> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getDisplayName(), newList.get(newPos).getDisplayName());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Displayable oldItem = items.get(oldPos);
                Displayable newItem = newList.get(newPos);
                return Objects.equals(oldItem.getDisplayStatus(), newItem.getDisplayStatus()) &&
                       Objects.equals(oldItem.getDisplayInfo(), newItem.getDisplayInfo()) &&
                       oldItem.isRunning() == newItem.isRunning() &&
                       oldItem.hasUpdate() == newItem.hasUpdate();
            }
        });
        items = new ArrayList<>(newList);
        res.dispatchUpdatesTo(this);
    }

    public void setActionListener(ActionListener l) {
        this.actionListener = l;
    }

    public void setUpdateListener(UpdateListener l) {
        this.updateListener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vm, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        Displayable item = items.get(pos);
        holder.name.setText(item.getDisplayName());
        
        String status = item.getDisplayStatus();
        String local = status;
        if ("RUNNING".equalsIgnoreCase(status)) local = holder.itemView.getContext().getString(R.string.status_running);
        else if ("STOPPED".equalsIgnoreCase(status)) local = holder.itemView.getContext().getString(R.string.status_stopped);
        else if ("OFFLINE".equalsIgnoreCase(status)) local = holder.itemView.getContext().getString(R.string.status_offline);
        else if ("ONLINE".equalsIgnoreCase(status)) local = holder.itemView.getContext().getString(R.string.status_online);

        holder.info.setText(String.format("%s • %s", local, item.getDisplayInfo()));
        holder.btnAction.setText(item.isRunning() ? R.string.action_stop : R.string.action_start);
        
        if (item.isRunning()) {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("STOPPED".equalsIgnoreCase(item.getDisplayStatus()) || "OFFLINE".equalsIgnoreCase(item.getDisplayStatus())) {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.info.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline));
        }

        if (item.hasUpdate()) {
            holder.badge.setVisibility(View.VISIBLE);
            holder.btnUpdate.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
            holder.btnUpdate.setVisibility(View.GONE);
        }
        
        holder.btnAction.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onAction(item);
        });

        holder.btnUpdate.setOnClickListener(v -> {
            if (updateListener != null) updateListener.onUpdate(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, info, badge;
        Button btnAction, btnUpdate;

        public Holder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.entity_name);
            info = view.findViewById(R.id.entity_info);
            badge = view.findViewById(R.id.update_badge);
            btnAction = view.findViewById(R.id.btn_action);
            btnUpdate = view.findViewById(R.id.btn_update);
        }
    }
}
