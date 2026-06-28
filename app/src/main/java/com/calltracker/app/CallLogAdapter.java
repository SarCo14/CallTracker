package com.calltracker.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    private final List<CallEntry> entries;
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.FRENCH);

    public CallLogAdapter(List<CallEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallEntry entry = entries.get(position);
        holder.tvName.setText(entry.name);
        holder.tvTime.setText(timeFmt.format(new Date(entry.date)));
        String duration = entry.getFormattedDuration();
        if (!duration.isEmpty()) { holder.tvDuration.setText(duration); holder.tvDuration.setVisibility(View.VISIBLE); }
        else holder.tvDuration.setVisibility(View.GONE);
        switch (entry.type) {
            case "in":
                holder.tvType.setText("Entrant");
                holder.tvType.setBackgroundResource(R.drawable.badge_in);
                holder.tvType.setTextColor(holder.itemView.getContext().getColor(R.color.blue));
                holder.vIndicator.setBackgroundResource(R.color.blue);
                holder.tvIcon.setText("↙");
                break;
            case "out":
                holder.tvType.setText("Sortant");
                holder.tvType.setBackgroundResource(R.drawable.badge_out);
                holder.tvType.setTextColor(holder.itemView.getContext().getColor(R.color.green));
                holder.vIndicator.setBackgroundResource(R.color.green);
                holder.tvIcon.setText("↗");
                break;
            case "miss":
                holder.tvType.setText("Manqué");
                holder.tvType.setBackgroundResource(R.drawable.badge_miss);
                holder.tvType.setTextColor(holder.itemView.getContext().getColor(R.color.red));
                holder.vIndicator.setBackgroundResource(R.color.red);
                holder.tvIcon.setText("✕");
                break;
        }
    }

    @Override
    public int getItemCount() { return entries.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvType, tvDuration, tvIcon;
        View vIndicator;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_call_name);
            tvTime = itemView.findViewById(R.id.tv_call_time);
            tvType = itemView.findViewById(R.id.tv_call_type);
            tvDuration = itemView.findViewById(R.id.tv_call_duration);
            tvIcon = itemView.findViewById(R.id.tv_call_icon);
            vIndicator = itemView.findViewById(R.id.v_indicator);
        }
    }
}
