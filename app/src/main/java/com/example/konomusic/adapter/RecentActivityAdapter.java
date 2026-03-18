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
import com.example.konomusic.model.RecentActivity;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private final List<RecentActivity> items;

    public RecentActivityAdapter(List<RecentActivity> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = items.get(position);
        holder.tvTitle.setText(activity.getTitle());
        holder.tvSubtitle.setText(activity.getSubtitle());
        holder.tvTime.setText(activity.getTimestamp());

        // Set icon & color by type
        switch (activity.getType()) {
            case RecentActivity.TYPE_UPLOAD:
                holder.ivType.setImageResource(R.drawable.ic_upload);
                holder.ivType.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF55EFC4));
                break;
            case RecentActivity.TYPE_DELETE:
                holder.ivType.setImageResource(R.drawable.ic_delete);
                holder.ivType.setBackgroundTintList(
                        ColorStateList.valueOf(0xFFFF7675));
                break;
            case RecentActivity.TYPE_EDIT:
                holder.ivType.setImageResource(R.drawable.ic_edit);
                holder.ivType.setBackgroundTintList(
                        ColorStateList.valueOf(0xFFFDCB6E));
                break;
            case RecentActivity.TYPE_USER:
                holder.ivType.setImageResource(R.drawable.ic_person_add);
                holder.ivType.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF74B9FF));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivType;
        TextView tvTitle, tvSubtitle, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.iv_activity_type);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvSubtitle = itemView.findViewById(R.id.tv_activity_subtitle);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
        }
    }
}

