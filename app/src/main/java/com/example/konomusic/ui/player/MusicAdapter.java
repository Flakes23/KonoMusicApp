package com.example.konomusic.ui.player;

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
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.common.SongActionsHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {

    private final Context mContext;
    static ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles){
        this.mFiles = mFiles;
        this.mContext = mContext;
    }

    @NonNull
    @NotNull
    @Override
    public MusicAdapter.MyVieHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MusicAdapter.MyVieHolder holder, int position) {
        MusicFiles current = mFiles.get(position);
        holder.file_name.setText(current.getTitle());
        holder.artist_name.setText(current.getArtist());

        String artworkUrl = current.getArtworkUrl();
        if (current.isFromFirebase() && artworkUrl != null && !artworkUrl.trim().isEmpty()) {
            Glide.with(mContext)
                    .load(artworkUrl)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.album_art);
        } else {
            byte[] image = getAlbumArt(current.getPath());
            if (image != null){
                Glide.with(mContext).asBitmap()
                        .load(image)
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.album_art);
            }
            else {
                Glide.with(mContext)
                        .load(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.album_art);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) {
                return;
            }

            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("musicAdapter", "MusicAdapt");
            intent.putExtra("positionMfiles", adapterPos);
            mContext.startActivity(intent);

            if (NowPlayingFragmentBottom.playPauseBtn != null) {
                boolean isPlaying = true;
                if (PlayerActivity.passMusicService != null) {
                    try {
                        isPlaying = PlayerActivity.passMusicService.isPlaying();
                    } catch (Exception ignored) {
                    }
                }
                NowPlayingFragmentBottom.playPauseBtn.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            SongActionsHelper.showActions(mContext, current);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder{

        TextView file_name, artist_name;
        ImageView album_art;

        public MyVieHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            artist_name = itemView.findViewById(R.id.music_artist_name);
            album_art = itemView.findViewById(R.id.music_img);
        }
    }

    private byte[] getAlbumArt(String uri){
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

    public void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }


    public static void setCurrentList(ArrayList<MusicFiles> files) {
        mFiles = files;
    }

    public static ArrayList<MusicFiles> getCurrentList() {
        return mFiles;
    }
}
