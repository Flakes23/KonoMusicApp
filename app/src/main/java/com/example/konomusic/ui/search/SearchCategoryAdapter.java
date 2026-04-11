package com.example.konomusic.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.domain.model.CategoryItem;

import java.util.ArrayList;

public class SearchCategoryAdapter extends RecyclerView.Adapter<SearchCategoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(CategoryItem item);
    }

    private final ArrayList<CategoryItem> items;
    private final OnItemClickListener listener;

    public SearchCategoryAdapter(ArrayList<CategoryItem> items, OnItemClickListener listener) {
        this.items = items == null ? new ArrayList<>() : items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = items.get(position);
        holder.name.setText(item.getName());

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.image);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.searchCategoryImage);
            name = itemView.findViewById(R.id.searchCategoryName);
        }
    }
}
