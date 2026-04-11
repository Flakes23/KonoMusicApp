package com.example.konomusic.ui.album;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.konomusic.playback.MusicService;
import com.example.konomusic.ui.common.SongActionsHelper;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.PlayerActivity;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.konomusic.ui.player.NowPlayingFragmentBottom.ARTWORK_URL;

public class CategoryMusicAdapter extends RecyclerView.Adapter<CategoryMusicAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MusicFiles> songs;

    public CategoryMusicAdapter(Context context, ArrayList<MusicFiles> songs) {
        this.context = context;
        this.songs = songs == null ? new ArrayList<>() : songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicFiles current = songs.get(position);
        holder.fileName.setText(current.getTitle());
        holder.artistName.setText(current.getArtist());

        String artworkUrl = current.getArtworkUrl();
        if (current.isFromFirebase() && artworkUrl != null && !artworkUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(artworkUrl)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.albumArt);
        } else {
            byte[] image = getAlbumArt(current.getPath());
            if (image != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(image)
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.albumArt);
            } else {
                Glide.with(context)
                        .load(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.albumArt);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int targetIndex = findIndexInMainList(current);

            SharedPreferences.Editor editor = context.getSharedPreferences(MusicService.MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
            editor.putString(MusicService.MUSIC_FILE, current.getPath());
            editor.putString(MusicService.ARTIST_NAME, current.getArtist());
            editor.putString(MusicService.SONG_NAME, current.getTitle());
            editor.putString(ARTWORK_URL, current.getArtworkUrl());
            editor.apply();

            if (targetIndex >= 0) {
                MusicAdapter.setCurrentList(MainActivity.getMusicFiles());
            } else {
                targetIndex = holder.getAdapterPosition();
                MusicAdapter.setCurrentList(songs);
            }

            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("musicAdapter", "MusicAdapt");
            intent.putExtra("positionMfiles", targetIndex);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            SongActionsHelper.showActions(context, current);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView artistName;
        ImageView albumArt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.music_file_name);
            artistName = itemView.findViewById(R.id.music_artist_name);
            albumArt = itemView.findViewById(R.id.music_img);
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
}
