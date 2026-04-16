package com.example.konomusic.ui.home;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
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

public class HomeSongAdapter extends RecyclerView.Adapter<HomeSongAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MusicFiles> songs;

    public HomeSongAdapter(Context context, ArrayList<MusicFiles> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_song_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicFiles song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.sub.setText(song.getArtist());

        String artworkUrl = song.getArtworkUrl();
        if (song.isFromFirebase() && artworkUrl != null && !artworkUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(artworkUrl)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.cover);
        } else {
            byte[] image = getAlbumArt(song.getPath());
            if (image != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(image)
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.cover);
            } else {
                Glide.with(context)
                        .load(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.cover);
            }
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
        return songs == null ? 0 : songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView sub;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.homeSongImage);
            title = itemView.findViewById(R.id.homeSongTitle);
            sub = itemView.findViewById(R.id.homeSongSub);
        }
    }

    private byte[] getAlbumArt(String uri) {
        if (uri == null) {
            return null;
        }
        String lower = uri.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            return retriever.getEmbeddedPicture();
        } catch (RuntimeException e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
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
}
