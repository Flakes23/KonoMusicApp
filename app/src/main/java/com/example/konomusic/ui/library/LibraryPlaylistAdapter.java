package com.example.konomusic.ui.library;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.data.repository.UserLibraryRepository.PlaylistItem;

import java.util.ArrayList;

public class LibraryPlaylistAdapter extends RecyclerView.Adapter<LibraryPlaylistAdapter.ViewHolder> {

    public interface PlaylistActionListener {
        void onLongPress(PlaylistItem item);
    }

    private final ArrayList<PlaylistItem> playlists;
    private final PlaylistActionListener actionListener;

    public LibraryPlaylistAdapter(ArrayList<PlaylistItem> playlists,
                                  PlaylistActionListener actionListener) {
        this.playlists = playlists == null ? new ArrayList<>() : playlists;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistItem item = playlists.get(position);
        holder.name.setText(item.getName());
        holder.meta.setText(holder.itemView.getContext().getString(R.string.library_playlist_song_count, item.getSongCount()));

        String cover = item.getCoverUrl();
        if (cover != null && !cover.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(cover)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.cover);
        } else {
            Glide.with(holder.itemView.getContext()).load(R.drawable.musicicon).centerCrop().into(holder.cover);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PlaylistDetailActivity.class);
            intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_ID, item.getPlaylistId());
            intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_NAME, item.getName());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onLongPress(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView name;
        TextView meta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.libraryPlaylistCover);
            name = itemView.findViewById(R.id.libraryPlaylistName);
            meta = itemView.findViewById(R.id.libraryPlaylistMeta);
        }
    }
}
