package com.dmitrij.behappy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.AlertInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.Holder> {
    private List<AlertInfo> items = new ArrayList<>();

    public void setAlerts(List<AlertInfo> list) {
        final List<AlertInfo> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getFormatted(), newList.get(newPos).getFormatted());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getLevel(), newList.get(newPos).getLevel());
            }
        });
        items = new ArrayList<>(newList);
        res.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AlertInfo info = items.get(position);
        holder.text.setText(info.getFormatted());
        holder.level.setText(info.getLevel());

        int color;
        int icon;

        String level = info.getLevel().toUpperCase();
        if (level.contains("CRITICAL") || level.contains("ERROR") || level.contains("FATAL")) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_red);
            icon = android.R.drawable.stat_notify_error;
        } else if (level.contains("WARN")) {
            color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark);
            icon = android.R.drawable.stat_sys_warning;
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.tg_blue);
            icon = android.R.drawable.stat_notify_chat;
        }

        holder.level.setTextColor(color);
        holder.icon.setImageResource(icon);
        holder.icon.setColorFilter(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text, level;

        public Holder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.alert_icon);
            text = itemView.findViewById(R.id.alert_text);
            level = itemView.findViewById(R.id.alert_level);
        }
    }
}
