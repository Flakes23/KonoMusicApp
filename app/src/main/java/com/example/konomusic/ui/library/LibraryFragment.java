package com.example.konomusic.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.auth.AuthManager;
import com.example.konomusic.data.repository.UserLibraryRepository;
import com.example.konomusic.data.repository.UserLibraryRepository.PlaylistItem;
import com.example.konomusic.domain.model.MusicFiles;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class LibraryFragment extends Fragment {

    private static LibraryFragment instance;

    private com.example.konomusic.data.repository.UserLibraryRepository repository;

    private View guestBox;
    private com.google.android.material.button.MaterialButton loginBtn;
    private RecyclerView favoritesRecycler;
    private RecyclerView playlistsRecycler;
    private TextView favoritesEmpty;
    private TextView playlistsEmpty;

    public LibraryFragment() {
    }

    public static void refreshContent() {
        if (instance != null) {
            instance.loadData();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        instance = this;
        repository = new com.example.konomusic.data.repository.UserLibraryRepository();

        guestBox = view.findViewById(R.id.libraryGuestBox);
        loginBtn = view.findViewById(R.id.libraryLoginBtn);
        favoritesRecycler = view.findViewById(R.id.libraryFavoritesRecycler);
        playlistsRecycler = view.findViewById(R.id.libraryPlaylistsRecycler);
        favoritesEmpty = view.findViewById(R.id.libraryFavoritesEmpty);
        playlistsEmpty = view.findViewById(R.id.libraryPlaylistsEmpty);

        favoritesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        loginBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                AuthManager.openLogin(getActivity());
            }
        });

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded()) {
            return;
        }

        FirebaseUser user = AuthManager.currentUser();
        if (user == null) {
            guestBox.setVisibility(View.VISIBLE);
            favoritesRecycler.setAdapter(new LibraryFavoriteAdapter(requireContext(), new ArrayList<>()));
            playlistsRecycler.setAdapter(new LibraryPlaylistAdapter(new ArrayList<>(), null));
            favoritesEmpty.setVisibility(View.VISIBLE);
            playlistsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        guestBox.setVisibility(View.GONE);

        repository.loadFavorites(user.getUid(), new com.example.konomusic.data.repository.UserLibraryRepository.FavoritesCallback() {
            @Override
            public void onResult(ArrayList<MusicFiles> songs) {
                if (!isAdded()) {
                    return;
                }
                favoritesRecycler.setAdapter(new LibraryFavoriteAdapter(requireContext(), songs));
                favoritesEmpty.setVisibility(songs == null || songs.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                favoritesRecycler.setAdapter(new LibraryFavoriteAdapter(requireContext(), new ArrayList<>()));
                favoritesEmpty.setVisibility(View.VISIBLE);
            }
        });

        repository.loadPlaylists(user.getUid(), new com.example.konomusic.data.repository.UserLibraryRepository.PlaylistsCallback() {
            @Override
            public void onResult(ArrayList<PlaylistItem> playlists) {
                if (!isAdded()) {
                    return;
                }
                playlistsRecycler.setAdapter(new LibraryPlaylistAdapter(playlists, LibraryFragment.this::showPlaylistActions));
                playlistsEmpty.setVisibility(playlists == null || playlists.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                playlistsRecycler.setAdapter(new LibraryPlaylistAdapter(new ArrayList<>(), null));
                playlistsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showPlaylistActions(PlaylistItem item) {
        if (!isAdded() || item == null) {
            return;
        }

        String[] actions = new String[]{
                getString(R.string.library_rename_playlist),
                getString(R.string.library_delete_playlist)
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(item.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(item);
                    } else {
                        deletePlaylist(item);
                    }
                })
                .show();
    }

    private void showRenameDialog(PlaylistItem item) {
        if (!isAdded() || item == null) {
            return;
        }

        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_playlist_name, null, false);
        TextInputEditText input = content.findViewById(R.id.playlistNameInput);
        TextView subtitle = content.findViewById(R.id.playlistNameDialogSubtitle);
        input.setText(item.getName());
        subtitle.setText(getString(R.string.playlist_rename_dialog_subtitle));

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_rename_playlist)
                .setView(content)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    FirebaseUser user = AuthManager.currentUser();
                    if (user == null) {
                        return;
                    }
                    String newName = input.getText() == null ? "" : input.getText().toString().trim();
                    repository.renamePlaylist(user.getUid(), item.getPlaylistId(), newName, new com.example.konomusic.data.repository.UserLibraryRepository.ResultCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), R.string.library_rename_success, Toast.LENGTH_SHORT).show();
                            loadData();
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deletePlaylist(PlaylistItem item) {
        if (!isAdded() || item == null) {
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_delete_playlist)
                .setMessage(getString(R.string.library_delete_playlist_confirm, item.getName()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    FirebaseUser user = AuthManager.currentUser();
                    if (user == null) {
                        return;
                    }
                    repository.deletePlaylist(user.getUid(), item.getPlaylistId(), new com.example.konomusic.data.repository.UserLibraryRepository.ResultCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), R.string.library_delete_success, Toast.LENGTH_SHORT).show();
                            loadData();
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
