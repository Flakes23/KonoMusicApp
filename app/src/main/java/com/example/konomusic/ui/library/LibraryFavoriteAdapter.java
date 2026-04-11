package com.example.konomusic.ui.library;

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
import com.example.konomusic.data.repository.UserLibraryRepository;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.PlayerActivity;

import java.util.ArrayList;

public class LibraryFavoriteAdapter extends RecyclerView.Adapter<LibraryFavoriteAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MusicFiles> songs;

    public LibraryFavoriteAdapter(Context context, ArrayList<MusicFiles> songs) {
        this.context = context;
        this.songs = songs == null ? new ArrayList<>() : songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_library_favorite, parent, false);
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
            int target = findIndexInMain(song);
            if (target >= 0) {
                MusicAdapter.setCurrentList(MainActivity.getMusicFiles());
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("musicAdapter", "MusicAdapt");
                intent.putExtra("positionMfiles", target);
                context.startActivity(intent);
                return;
            }

            MusicAdapter.setCurrentList(songs);
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("musicAdapter", "MusicAdapt");
            intent.putExtra("positionMfiles", holder.getAdapterPosition());
            context.startActivity(intent);
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.libraryFavoriteCover);
            title = itemView.findViewById(R.id.libraryFavoriteTitle);
            subtitle = itemView.findViewById(R.id.libraryFavoriteSubtitle);
        }
    }

    private int findIndexInMain(MusicFiles selected) {
        ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
        if (mainSongs == null || selected == null) {
            return -1;
        }

        String key = UserLibraryRepository.songKey(selected);
        for (int i = 0; i < mainSongs.size(); i++) {
            MusicFiles song = mainSongs.get(i);
            if (song == null) {
                continue;
            }
            String mainKey = UserLibraryRepository.songKey(song);
            if (!key.isEmpty() && key.equals(mainKey)) {
                return i;
            }
        }
        return -1;
    }
}
