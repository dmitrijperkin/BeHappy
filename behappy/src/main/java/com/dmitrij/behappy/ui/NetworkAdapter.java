package com.dmitrij.behappy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.NetworkInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.Holder> {
    private List<NetworkInterface> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onToggle(String name, boolean en);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setInterfaces(List<NetworkInterface> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        NetworkInterface item = items.get(pos);
        
        String name = item.getName();
        holder.name.setText(name != null ? name : "Unknown Interface");
        
        String type = item.getType();
        holder.type.setText(type != null ? type : "Unknown Type");
        
        String state = item.getLinkState();
        if (state == null) state = "UNKNOWN";
        holder.status.setText(state);
        
        boolean up = state.toUpperCase().contains("UP");
        int color = up ? 
                holder.itemView.getContext().getColor(R.color.accent_green) : 
                holder.itemView.getContext().getColor(R.color.accent_red);
        
        holder.status.setTextColor(color);
        holder.dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        
        holder.sw.setOnCheckedChangeListener(null);
        holder.sw.setChecked(up);
        
        final android.content.Context context = holder.itemView.getContext();
        
        holder.sw.setOnCheckedChangeListener((v, en) -> {
            item.setLinkState(en ? "LINK_STATE_UP" : "LINK_STATE_DOWN");

            String newState = en ? "LINK_STATE_UP" : "LINK_STATE_DOWN";
            holder.status.setText(newState);
            
            int newColor = en ? 
                    context.getColor(R.color.accent_green) : 
                    context.getColor(R.color.accent_red);

            holder.status.setTextColor(newColor);
            holder.dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(newColor));

            if (listener != null && name != null) {
                listener.onToggle(name, en);
            }
        });

        if (item.getAliases() != null) {
            String list = item.getAliases().stream()
                    .map(NetworkInterface.Alias::getAddress)
                    .collect(Collectors.joining(", "));
            holder.addr.setText(list);
        } else {
            holder.addr.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, type, status, addr;
        View dot;
        com.google.android.material.materialswitch.MaterialSwitch sw;

        public Holder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.interface_name);
            type = view.findViewById(R.id.interface_type);
            status = view.findViewById(R.id.interface_status);
            addr = view.findViewById(R.id.interface_addresses);
            dot = view.findViewById(R.id.status_dot);
            sw = view.findViewById(R.id.interface_switch);
        }
    }
}
