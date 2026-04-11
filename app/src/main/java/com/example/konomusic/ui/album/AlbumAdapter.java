package com.example.konomusic.ui.album;

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

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> albumFiles;
    View view;

    public AlbumAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        MusicFiles albumItem = albumFiles.get(position);
        holder.album_name.setText(albumItem.getAlbum());
        holder.artist_name.setText(albumItem.getArtist());

        String imageUrl = firstNonEmpty(albumItem.getAlbumImageUrl(), albumItem.getArtworkUrl());
        if (!imageUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.musicicon)
                    .error(R.drawable.musicicon)
                    .centerCrop()
                    .into(holder.album_image);
        } else {
            byte[] image = getAlbumArtSafely(albumItem.getPath());
            if (image != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(image)
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.album_image);
            } else {
                Glide.with(mContext)
                        .load(R.drawable.musicicon)
                        .centerCrop()
                        .into(holder.album_image);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            MusicFiles selectedAlbum = albumFiles.get(adapterPosition);
            Intent intent = new Intent(mContext, CategoryDetailActivity.class);
            intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_TYPE, "album");
            intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, firstNonEmpty(selectedAlbum.getPrimaryAlbumId(), selectedAlbum.getAlbum()));
            intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_VALUE, firstNonEmpty(selectedAlbum.getAlbum(), "Album"));
            intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_IMAGE,
                    firstNonEmpty(selectedAlbum.getAlbumImageUrl(), selectedAlbum.getArtworkUrl()));
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView album_image;
        TextView album_name, artist_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
            artist_name = itemView.findViewById(R.id.artist_name);
        }
    }

    private byte[] getAlbumArtSafely(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            return null;
        }
        // Remote streams usually do not provide embedded artwork and may throw at setDataSource.
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
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

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
