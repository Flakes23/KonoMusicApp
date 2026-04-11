package com.example.konomusic.ui.common;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.konomusic.R;
import com.example.konomusic.auth.AuthManager;
import com.example.konomusic.data.repository.UserLibraryRepository;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.library.PlaylistDialogUi;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public final class SongActionsHelper {

    private SongActionsHelper() {
    }

    public static void showActions(Context context, MusicFiles song) {
        if (context == null || song == null) {
            return;
        }

        String[] items = new String[]{
                context.getString(R.string.player_favorite),
                context.getString(R.string.player_add_to_playlist)
        };

        new AlertDialog.Builder(context)
                .setTitle(R.string.song_actions_title)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        toggleFavorite(context, song);
                    } else {
                        addToPlaylist(context, song);
                    }
                })
                .show();
    }

    private static void toggleFavorite(Context context, MusicFiles song) {
        FirebaseUser user = AuthManager.currentUser();
        if (user == null) {
            requireLogin(context);
            return;
        }

        UserLibraryRepository repository = new UserLibraryRepository();
        repository.toggleFavorite(user.getUid(), song, new UserLibraryRepository.FavoriteStateCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                Toast.makeText(context,
                        context.getString(isFavorite ? R.string.player_favorite_added : R.string.player_favorite_removed),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void addToPlaylist(Context context, MusicFiles song) {
        FirebaseUser user = AuthManager.currentUser();
        if (user == null) {
            requireLogin(context);
            return;
        }

        UserLibraryRepository repository = new UserLibraryRepository();
        repository.loadPlaylists(user.getUid(), new UserLibraryRepository.PlaylistsCallback() {
            @Override
            public void onResult(ArrayList<UserLibraryRepository.PlaylistItem> playlists) {
                showPlaylistDialog(context, repository, user.getUid(), song, playlists);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void showPlaylistDialog(Context context,
                                           UserLibraryRepository repository,
                                           String uid,
                                           MusicFiles song,
                                           ArrayList<UserLibraryRepository.PlaylistItem> playlists) {
        PlaylistDialogUi.showPicker(
                context,
                playlists,
                () -> showCreatePlaylistDialog(context, repository, uid, song),
                (selected, onCountUpdated) -> repository.addSongToPlaylist(uid, selected.getPlaylistId(), song, new UserLibraryRepository.ResultCallback() {
                    @Override
                    public void onSuccess() {
                        if (onCountUpdated != null) {
                            onCountUpdated.run();
                        }
                        Toast.makeText(context, R.string.player_playlist_added, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private static void showCreatePlaylistDialog(Context context,
                                                 UserLibraryRepository repository,
                                                 String uid,
                                                 MusicFiles song) {
        PlaylistDialogUi.showNameInput(
                context,
                context.getString(R.string.player_playlist_new_title),
                context.getString(R.string.playlist_name_dialog_subtitle),
                "",
                name -> repository.createPlaylistAndAddSong(uid, name, song, new UserLibraryRepository.ResultCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, R.string.player_playlist_created_and_added, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private static void requireLogin(Context context) {
        Toast.makeText(context, R.string.player_login_required, Toast.LENGTH_SHORT).show();
        if (context instanceof Activity) {
            AuthManager.openLogin((Activity) context);
        }
    }
}
