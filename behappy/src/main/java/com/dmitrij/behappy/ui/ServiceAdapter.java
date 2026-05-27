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
import com.dmitrij.behappy.model.ServiceInfo;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.Holder> {
    private List<ServiceInfo> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onAction(ServiceInfo info);
    }

    public void setServices(List<ServiceInfo> list) {
        final List<ServiceInfo> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getId(), newList.get(newPos).getId());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        ServiceInfo info = items.get(pos);
        holder.name.setText(info.getService());
        
        String state = info.getState();
        String statusText = state;
        if ("RUNNING".equalsIgnoreCase(state)) statusText = holder.itemView.getContext().getString(R.string.status_running);
        else if ("STOPPED".equalsIgnoreCase(state)) statusText = holder.itemView.getContext().getString(R.string.status_stopped);
        
        holder.status.setText(statusText);
        
        boolean active = "RUNNING".equalsIgnoreCase(info.getState());
        holder.btn.setText(active ? R.string.action_stop : R.string.action_start);

        if (active) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("STOPPED".equalsIgnoreCase(info.getState())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline));
        }
        
        holder.btn.setOnClickListener(v -> {
            if (listener != null) listener.onAction(info);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, status;
        Button btn;

        public Holder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.service_name);
            status = view.findViewById(R.id.service_status);
            btn = view.findViewById(R.id.btn_action);
        }
    }
}
