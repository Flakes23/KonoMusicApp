package com.example.konomusic.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.model.StatCard;

import java.util.List;

public class StatCardAdapter extends RecyclerView.Adapter<StatCardAdapter.ViewHolder> {

    private final List<StatCard> items;

    public StatCardAdapter(List<StatCard> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatCard card = items.get(position);

        holder.ivIcon.setImageResource(card.getIconResId());
        holder.ivIcon.setBackgroundTintList(ColorStateList.valueOf(card.getAccentColor()));

        holder.tvCount.setText(card.getCount());
        holder.tvLabel.setText(card.getLabel());
        holder.tvTrend.setText(card.getTrend());
        holder.accentLine.setBackgroundColor(card.getAccentColor());

        // Trend badge background color
        int trendColor = card.isTrendUp() ? 0xFF55EFC4 : 0xFFFF7675;
        holder.tvTrend.setBackgroundTintList(ColorStateList.valueOf(trendColor));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCount, tvLabel, tvTrend;
        View accentLine;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_stat_icon);
            tvCount = itemView.findViewById(R.id.tv_stat_count);
            tvLabel = itemView.findViewById(R.id.tv_stat_label);
            tvTrend = itemView.findViewById(R.id.tv_trend);
            accentLine = itemView.findViewById(R.id.accent_line);
        }
    }
}

