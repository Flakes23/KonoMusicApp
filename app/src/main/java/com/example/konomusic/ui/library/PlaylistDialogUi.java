package com.example.konomusic.ui.library;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.data.repository.UserLibraryRepository.PlaylistItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public final class PlaylistDialogUi {

    public interface OnCreateNewClick {
        void onCreateNew();
    }

    public interface OnPlaylistClick {
        void onPlaylist(PlaylistItem item, Runnable onCountUpdated);
    }

    public interface OnNameConfirm {
        void onConfirm(String name);
    }

    private PlaylistDialogUi() {
    }

    public static void showPicker(Context context,
                                  ArrayList<PlaylistItem> playlists,
                                  OnCreateNewClick createNewClick,
                                  OnPlaylistClick playlistClick) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_add_playlist, null, false);
        RecyclerView recyclerView = content.findViewById(R.id.dialogPlaylistRecycler);
        TextView emptyText = content.findViewById(R.id.dialogPlaylistEmpty);
        MaterialButton createBtn = content.findViewById(R.id.dialogCreatePlaylistBtn);
        MaterialButton cancelBtn = content.findViewById(R.id.dialogCancelBtn);

        ArrayList<PlaylistItem> items = playlists == null ? new ArrayList<>() : playlists;
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new PlaylistListAdapter(items, playlistClick));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .create();

        createBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (createNewClick != null) {
                createNewClick.onCreateNew();
            }
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        applyDialogWindowStyle(dialog);
        dialog.show();
    }

    public static void showNameInput(Context context, String title, String subtitle, String initialValue, OnNameConfirm onNameConfirm) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_playlist_name, null, false);
        TextView titleView = content.findViewById(R.id.playlistNameDialogTitle);
        TextView subtitleView = content.findViewById(R.id.playlistNameDialogSubtitle);
        TextInputEditText input = content.findViewById(R.id.playlistNameInput);
        MaterialButton cancelBtn = content.findViewById(R.id.playlistNameCancelBtn);
        MaterialButton okBtn = content.findViewById(R.id.playlistNameOkBtn);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        if (initialValue != null) {
            input.setText(initialValue);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        okBtn.setOnClickListener(v -> {
            String name = input.getText() == null ? "" : input.getText().toString().trim();
            if (onNameConfirm != null) {
                onNameConfirm.onConfirm(name);
            }
            dialog.dismiss();
        });

        applyDialogWindowStyle(dialog);
        dialog.show();
    }

    private static void applyDialogWindowStyle(AlertDialog dialog) {
        if (dialog == null) {
            return;
        }
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setDimAmount(0.75f);
    }

    private static class PlaylistListAdapter extends RecyclerView.Adapter<PlaylistListAdapter.Holder> {
        private final ArrayList<PlaylistItem> items;
        private final OnPlaylistClick click;

        PlaylistListAdapter(ArrayList<PlaylistItem> items, OnPlaylistClick click) {
            this.items = items == null ? new ArrayList<>() : items;
            this.click = click;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_dialog, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PlaylistItem item = items.get(position);
            holder.name.setText(item.getName());
            holder.meta.setText(holder.itemView.getContext().getString(R.string.library_playlist_song_count, item.getSongCount()));
            holder.itemView.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) {
                    return;
                }
                PlaylistItem selected = items.get(adapterPos);
                if (click != null) {
                    click.onPlaylist(selected, () -> incrementSongCount(adapterPos));
                }
            });
        }

        private void incrementSongCount(int index) {
            if (index < 0 || index >= items.size()) {
                return;
            }
            PlaylistItem current = items.get(index);
            int nextCount = Math.max(0, current.getSongCount() + 1);
            items.set(index, new PlaylistItem(
                    current.getPlaylistId(),
                    current.getName(),
                    nextCount,
                    current.getCoverUrl()
            ));
            notifyItemChanged(index);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView name;
            TextView meta;

            Holder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.dialogPlaylistName);
                meta = itemView.findViewById(R.id.dialogPlaylistMeta);
            }
        }
    }
}
