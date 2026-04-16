package com.example.konomusic.ui.library;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.example.konomusic.R;
import com.example.konomusic.auth.AuthManager;
import com.example.konomusic.data.repository.UserLibraryRepository;
import com.example.konomusic.domain.model.MusicFiles;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class PlaylistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "extra_playlist_name";

    private RecyclerView recyclerView;
    private TextView titleView;
    private TextView subtitleView;
    private TextView emptyView;

    private final UserLibraryRepository repository = new UserLibraryRepository();
    private ArrayList<MusicFiles> songs = new ArrayList<>();
    private String playlistId;
    private String playlistName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        ImageButton backBtn = findViewById(R.id.playlistDetailBack);
        MaterialButton deleteBtn = findViewById(R.id.playlistDetailDeleteBtn);
        titleView = findViewById(R.id.playlistDetailTitle);
        subtitleView = findViewById(R.id.playlistDetailSubtitle);
        emptyView = findViewById(R.id.playlistDetailEmpty);
        recyclerView = findViewById(R.id.playlistDetailRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        backBtn.setOnClickListener(v -> finish());
        deleteBtn.setOnClickListener(v -> confirmDeletePlaylist());

        playlistId = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);
        playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);

        titleView.setText(playlistName == null || playlistName.trim().isEmpty()
                ? getString(R.string.library_playlists_title)
                : playlistName.trim());

        loadSongs();
    }

    private void loadSongs() {
        FirebaseUser user = AuthManager.currentUser();
        if (user == null || playlistId == null || playlistId.trim().isEmpty()) {
            bindSongs(new ArrayList<>());
            return;
        }

        repository.loadPlaylistSongs(user.getUid(), playlistId.trim(), new UserLibraryRepository.PlaylistSongsCallback() {
            @Override
            public void onResult(ArrayList<MusicFiles> result) {
                bindSongs(result);
            }

            @Override
            public void onError(String message) {
                bindSongs(new ArrayList<>());
            }
        });
    }

    private void bindSongs(ArrayList<MusicFiles> result) {
        songs = result == null ? new ArrayList<>() : result;
        recyclerView.setAdapter(new PlaylistSongAdapter(this, songs, this::removeSong));
        boolean empty = songs.isEmpty();
        emptyView.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        subtitleView.setText(getString(R.string.library_playlist_song_count, songs.size()));
    }

    private void removeSong(MusicFiles song) {
        FirebaseUser user = AuthManager.currentUser();
        if (user == null || playlistId == null || playlistId.trim().isEmpty() || song == null) {
            return;
        }

        repository.removeSongFromPlaylist(user.getUid(), playlistId.trim(), song, new UserLibraryRepository.ResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(PlaylistDetailActivity.this, R.string.library_remove_song_success, Toast.LENGTH_SHORT).show();
                loadSongs();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PlaylistDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeletePlaylist() {
        FirebaseUser user = AuthManager.currentUser();
        if (user == null || playlistId == null || playlistId.trim().isEmpty()) {
            return;
        }

        String title = playlistName == null || playlistName.trim().isEmpty()
                ? getString(R.string.library_playlists_title)
                : playlistName.trim();

        View content = LayoutInflater.from(this).inflate(R.layout.dialog_delete_playlist, null, false);
        TextView messageView = content.findViewById(R.id.deleteDialogMessage);
        MaterialButton cancelBtn = content.findViewById(R.id.deleteDialogCancelBtn);
        MaterialButton deleteBtn = content.findViewById(R.id.deleteDialogConfirmBtn);
        messageView.setText(getString(R.string.library_delete_playlist_confirm, title));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        deleteBtn.setOnClickListener(v -> {
            repository.deletePlaylist(user.getUid(), playlistId.trim(), new UserLibraryRepository.ResultCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    Toast.makeText(PlaylistDetailActivity.this, R.string.library_delete_success, Toast.LENGTH_SHORT).show();
                    LibraryFragment.refreshContent();
                    finish();
                }

                @Override
                public void onError(String message) {
                    dialog.dismiss();
                    Toast.makeText(PlaylistDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.75f);
        }
    }
}
