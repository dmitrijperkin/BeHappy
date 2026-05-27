package com.dmitrij.behappy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.model.AuditEntry;

import java.util.ArrayList;
import java.util.List;

public class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.Holder> {

    private List<AuditEntry> items = new ArrayList<>();

    public void setItems(List<AuditEntry> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audit, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        AuditEntry item = items.get(pos);
        holder.s.setText(item.getService());
        holder.u.setText(item.getUser());
        holder.t.setText(item.getTimestamp());
        holder.e.setText(item.getEvent());
        holder.d.setText(item.getEventData());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView s, u, t, e, d;

        Holder(@NonNull View view) {
            super(view);
            s = view.findViewById(R.id.audit_service);
            u = view.findViewById(R.id.audit_user);
            t = view.findViewById(R.id.audit_timestamp);
            e = view.findViewById(R.id.audit_event);
            d = view.findViewById(R.id.audit_data);
        }
    }
}
