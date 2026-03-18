package com.example.konomusic.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.model.TopSong;

import java.util.List;

public class TopSongAdapter extends RecyclerView.Adapter<TopSongAdapter.ViewHolder> {

    private final List<TopSong> items;

    // Rank badge colors cycle
    private static final int[] RANK_COLORS = {
            0xFFFFD700, // gold
            0xFFC0C0C0, // silver
            0xFFCD7F32, // bronze
            0xFFE94560,
            0xFF74B9FF
    };

    public TopSongAdapter(List<TopSong> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopSong song = items.get(position);
        holder.tvRank.setText(String.valueOf(song.getRank()));
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvPlayCount.setText(song.getPlayCount());

        int color = RANK_COLORS[position % RANK_COLORS.length];
        holder.tvRank.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTitle, tvArtist, tvPlayCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            tvPlayCount = itemView.findViewById(R.id.tv_play_count);
        }
    }
}

