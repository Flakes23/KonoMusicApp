package com.example.konomusic.ui.library;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.PlayerActivity;

import java.util.ArrayList;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.ViewHolder> {

    public interface RemoveClickListener {
        void onRemove(MusicFiles song);
    }

    private final Context context;
    private final ArrayList<MusicFiles> songs;
    private final RemoveClickListener removeClickListener;

    public PlaylistSongAdapter(Context context, ArrayList<MusicFiles> songs, RemoveClickListener removeClickListener) {
        this.context = context;
        this.songs = songs == null ? new ArrayList<>() : songs;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicFiles song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.subtitle.setText(song.getArtist());

        String artwork = song.getArtworkUrl();
        if (artwork != null && !artwork.trim().isEmpty()) {
            Glide.with(context)
                    .load(artwork)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.cover);
        } else {
            Glide.with(context).load(R.drawable.musicicon).centerCrop().into(holder.cover);
        }

        holder.itemView.setOnClickListener(v -> {
            MusicAdapter.setCurrentList(songs);
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("musicAdapter", "MusicAdapt");
            intent.putExtra("positionMfiles", holder.getAdapterPosition());
            context.startActivity(intent);
        });

        holder.removeBtn.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemove(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView subtitle;
        ImageButton removeBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.playlistSongCover);
            title = itemView.findViewById(R.id.playlistSongTitle);
            subtitle = itemView.findViewById(R.id.playlistSongSub);
            removeBtn = itemView.findViewById(R.id.playlistSongRemoveBtn);
        }
    }
}
