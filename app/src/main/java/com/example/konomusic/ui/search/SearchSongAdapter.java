package com.example.konomusic.ui.search;

import android.content.Context;
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
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.common.SongActionsHelper;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.PlayerActivity;

import java.util.ArrayList;

public class SearchSongAdapter extends RecyclerView.Adapter<SearchSongAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MusicFiles> songs;

    public SearchSongAdapter(Context context, ArrayList<MusicFiles> songs) {
        this.context = context;
        this.songs = songs == null ? new ArrayList<>() : songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicFiles song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.sub.setText(buildSub(song));

        String artwork = song.getArtworkUrl();
        if (artwork != null && !artwork.trim().isEmpty()) {
            Glide.with(context)
                    .load(artwork)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.image);
        } else {
            Glide.with(context)
                    .load(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {

            int targetIndex = findIndexInMainList(song);
            if (targetIndex < 0) {
                targetIndex = holder.getAdapterPosition();
            }

            MusicAdapter.setCurrentList(MainActivity.getMusicFiles());
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("musicAdapter", "MusicAdapt");
            intent.putExtra("positionMfiles", targetIndex);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            SongActionsHelper.showActions(context, song);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private String buildSub(MusicFiles song) {
        String artist = song.getArtist() == null ? "Unknown Artist" : song.getArtist();
        String album = song.getAlbum() == null ? "Unknown Album" : song.getAlbum();
        return artist + " • " + album;
    }

    private int findIndexInMainList(MusicFiles selected) {
        ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
        if (mainSongs == null || selected == null) {
            return -1;
        }
        String id = selected.getId();
        String path = selected.getPath();
        for (int i = 0; i < mainSongs.size(); i++) {
            MusicFiles item = mainSongs.get(i);
            if (item == null) {
                continue;
            }
            if (id != null && !id.isEmpty() && id.equals(item.getId())) {
                return i;
            }
            if (path != null && path.equals(item.getPath())) {
                return i;
            }
        }
        return -1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView sub;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.searchSongImage);
            title = itemView.findViewById(R.id.searchSongTitle);
            sub = itemView.findViewById(R.id.searchSongSub);
        }
    }
}
