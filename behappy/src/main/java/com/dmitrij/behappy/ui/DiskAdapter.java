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
import com.dmitrij.behappy.model.DiskInfo;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DiskAdapter extends RecyclerView.Adapter<DiskAdapter.DiskViewHolder> {
    private List<DiskInfo> disks = new ArrayList<>();
    private OnDiskClickListener listener;

    public interface OnDiskClickListener {
        void onDiskClick(DiskInfo disk);
    }

    public void setListener(OnDiskClickListener listener) {
        this.listener = listener;
    }

    public void setDisks(List<DiskInfo> newDisks) {
        final List<DiskInfo> latest = newDisks != null ? newDisks : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return disks.size(); }
            @Override public int getNewListSize() { return latest.size(); }
            @Override public boolean areItemsTheSame(int oldP, int newP) {
                return Objects.equals(disks.get(oldP).getSerial(), latest.get(newP).getSerial());
            }
            @Override public boolean areContentsTheSame(int oldP, int newP) {
                return Objects.equals(disks.get(oldP).getTemperature(), latest.get(newP).getTemperature()) &&
                       Objects.equals(disks.get(oldP).getName(), latest.get(newP).getName());
            }
        });
        this.disks = new ArrayList<>(latest);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public DiskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disk, parent, false);
        return new DiskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiskViewHolder holder, int position) {
        DiskInfo disk = disks.get(position);
        holder.model.setText(disk.getModel());
        holder.nameSerial.setText(String.format("%s • SN: %s", disk.getName(), disk.getSerial()));
        holder.size.setText(formatSize(disk.getSize()));
        holder.type.setText(disk.getType());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDiskClick(disk);
        });

        if (disk.getTemperature() != null) {
            int temp = disk.getTemperature();
            holder.temp.setText(String.format(Locale.getDefault(), "%d°C", temp));
            
            if (temp < 40) {
                holder.temp.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
            } else if (temp < 50) {
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
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public int getItemCount() {
        return disks.size();
    }

    static class DiskViewHolder extends RecyclerView.ViewHolder {
        TextView model, nameSerial, size, type, temp;

        public DiskViewHolder(@NonNull View itemView) {
            super(itemView);
            model = itemView.findViewById(R.id.disk_model);
            nameSerial = itemView.findViewById(R.id.disk_name_serial);
            size = itemView.findViewById(R.id.disk_size);
            type = itemView.findViewById(R.id.disk_type);
            temp = itemView.findViewById(R.id.disk_temp);
        }
    }
}
