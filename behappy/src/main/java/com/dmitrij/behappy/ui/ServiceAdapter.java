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
import com.dmitrij.behappy.model.ServiceInfo;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<ServiceInfo> services = new ArrayList<>();
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onAction(ServiceInfo service);
    }

    public void setServices(List<ServiceInfo> newServices) {
        final List<ServiceInfo> latestServices = newServices != null ? newServices : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return services.size(); }
            @Override
            public int getNewListSize() { return latestServices.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(services.get(oldItemPosition).getId(), latestServices.get(newItemPosition).getId());
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(services.get(oldItemPosition), latestServices.get(newItemPosition));
            }
        });
        this.services = new ArrayList<>(latestServices);
        result.dispatchUpdatesTo(this);
    }

    public void setOnServiceActionListener(OnServiceActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceInfo service = services.get(position);
        holder.name.setText(service.getService());
        
        String state = service.getState();
        String translatedState = state;
        if ("RUNNING".equalsIgnoreCase(state)) translatedState = holder.itemView.getContext().getString(R.string.status_running);
        else if ("STOPPED".equalsIgnoreCase(state)) translatedState = holder.itemView.getContext().getString(R.string.status_stopped);
        
        holder.status.setText(translatedState);
        
        boolean isRunning = "RUNNING".equalsIgnoreCase(service.getState());
        holder.actionBtn.setText(isRunning ? R.string.action_stop : R.string.action_start);

        if (isRunning) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        } else if ("STOPPED".equalsIgnoreCase(service.getState())) {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
        } else {
            holder.status.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline));
        }
        
        holder.actionBtn.setOnClickListener(v -> {
            if (listener != null) listener.onAction(service);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        Button actionBtn;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.service_name);
            status = itemView.findViewById(R.id.service_status);
            actionBtn = itemView.findViewById(R.id.btn_action);
        }
    }
}
