package com.dmitrij.behappy.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.AlertInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
    private List<AlertInfo> alerts = new ArrayList<>();

    public void setAlerts(List<AlertInfo> newAlerts) {
        final List<AlertInfo> latestAlerts = newAlerts != null ? newAlerts : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return alerts.size(); }
            @Override
            public int getNewListSize() { return latestAlerts.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(alerts.get(oldItemPosition).getFormatted(), latestAlerts.get(newItemPosition).getFormatted());
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(alerts.get(oldItemPosition), latestAlerts.get(newItemPosition));
            }
        });
        this.alerts = new ArrayList<>(latestAlerts);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertInfo alert = alerts.get(position);
        Context context = holder.itemView.getContext();
        
        String formatted = alert.getFormatted();
        if ("Update available.".equalsIgnoreCase(formatted)) {
            formatted = context.getString(R.string.alert_update_available);
        } else if (formatted.startsWith("Scrub for pool '")) {
            String poolName = formatted.substring(16, formatted.indexOf("'", 16));
            formatted = context.getString(R.string.alert_scrub_finished, poolName);
        } else if (formatted.startsWith("The volume ") && formatted.contains(" state is ONLINE")) {
            String poolName = formatted.substring(11, formatted.indexOf(" ", 11));
            formatted = context.getString(R.string.alert_pool_online, poolName);
        } else if (formatted.startsWith("Task ") && formatted.endsWith(" failed.")) {
            String taskName = formatted.substring(5, formatted.length() - 8);
            formatted = context.getString(R.string.alert_task_failed, taskName);
        }
        else if ("System has been running stable for over 30 days. All services are normal.".equals(formatted) ||
            "Система работает стабильно более 30 дней. Все службы в норме.".equals(formatted)) {
            formatted = context.getString(R.string.demo_alert_stable);
        } else if ("Update available for Plex application.".equals(formatted) ||
                   "Доступно обновление для приложения Plex.".equals(formatted)) {
            formatted = context.getString(R.string.demo_alert_update);
        }

        holder.text.setText(formatted);

        String level = alert.getLevel();
        String translatedLevel = level;
        if ("INFO".equalsIgnoreCase(level)) translatedLevel = holder.itemView.getContext().getString(R.string.level_info);
        else if ("WARNING".equalsIgnoreCase(level)) translatedLevel = holder.itemView.getContext().getString(R.string.level_warning);
        else if ("CRITICAL".equalsIgnoreCase(level)) translatedLevel = holder.itemView.getContext().getString(R.string.level_critical);
        else if ("ERROR".equalsIgnoreCase(level)) translatedLevel = holder.itemView.getContext().getString(R.string.level_error);
        
        holder.level.setText(translatedLevel);

        if ("CRITICAL".equalsIgnoreCase(level) || "ERROR".equalsIgnoreCase(level)) {
            holder.level.setTextColor(0xFFFF4444);
        } else if ("WARNING".equalsIgnoreCase(level)) {
            holder.level.setTextColor(0xFFFFBB33);
        } else {
            holder.level.setTextColor(0xFF2481CC);
        }
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView text, level;
        ImageView icon;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.alert_text);
            level = itemView.findViewById(R.id.alert_level);
            icon = itemView.findViewById(R.id.alert_icon);
        }
    }
}
