package com.dmitrij.behappy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.DiskInfo;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DiskAdapter extends RecyclerView.Adapter<DiskAdapter.Holder> {
    private List<DiskInfo> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onDiskClick(DiskInfo info);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setDisks(List<DiskInfo> list) {
        final List<DiskInfo> newList = list != null ? list : new ArrayList<>();
        DiffUtil.DiffResult res = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getSerial(), newList.get(newPos).getSerial());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                return Objects.equals(items.get(oldPos).getTemperature(), newList.get(newPos).getTemperature()) &&
                       Objects.equals(items.get(oldPos).getName(), newList.get(newPos).getName());
            }
        });
        items = new ArrayList<>(newList);
        res.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disk, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        DiskInfo info = items.get(pos);
        holder.model.setText(info.getModel());
        holder.id.setText(String.format("%s • SN: %s", info.getName(), info.getSerial()));
        holder.size.setText(formatSize(info.getSize()));
        holder.type.setText(info.getType());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDiskClick(info);
        });

        if (info.getTemperature() != null) {
            int t = info.getTemperature();
            holder.temp.setText(String.format(Locale.getDefault(), "%d°C", t));
            
            if (t < 40) {
                holder.temp.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
            } else if (t < 50) {
                holder.temp.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiary));
            } else {
                holder.temp.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorError));
            }
            holder.temp.setVisibility(View.VISIBLE);
        } else {
            holder.temp.setVisibility(View.GONE);
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
        TextView model, id, size, type, temp;

        public Holder(@NonNull View view) {
            super(view);
            model = view.findViewById(R.id.disk_model);
            id = view.findViewById(R.id.disk_name_serial);
            size = view.findViewById(R.id.disk_size);
            type = view.findViewById(R.id.disk_type);
            temp = view.findViewById(R.id.disk_temp);
        }
    }
}
