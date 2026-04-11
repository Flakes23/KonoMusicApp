package com.example.konomusic.ui.home;

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

public class HomeTagAdapter extends RecyclerView.Adapter<HomeTagAdapter.ViewHolder> {

    public interface OnTagClickListener {
        void onTagClick(CategoryItem selectedTag);
    }

    private final ArrayList<CategoryItem> tags;
    private final OnTagClickListener onTagClickListener;

    public HomeTagAdapter(ArrayList<String> rawTags) {
        this(rawTags, null, null);
    }

    public HomeTagAdapter(ArrayList<String> rawTags, String type, OnTagClickListener onTagClickListener) {
        this.tags = new ArrayList<>();
        if (rawTags != null) {
            for (String name : rawTags) {
                tags.add(new CategoryItem(name, name, "", type));
            }
        }
        this.onTagClickListener = onTagClickListener;
    }

    public HomeTagAdapter(ArrayList<CategoryItem> tags, OnTagClickListener onTagClickListener) {
        this.tags = tags == null ? new ArrayList<>() : tags;
        this.onTagClickListener = onTagClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CategoryItem tag = tags.get(position);
        holder.text.setText(tag.getName());

        if (tag.getImageUrl() != null && !tag.getImageUrl().trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(tag.getImageUrl())
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.cover);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.cover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onTagClickListener != null) {
                onTagClickListener.onTagClick(tag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags == null ? 0 : tags.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView text;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.homeCategoryImage);
            text = itemView.findViewById(R.id.homeCategoryTitle);
        }
    }
}
