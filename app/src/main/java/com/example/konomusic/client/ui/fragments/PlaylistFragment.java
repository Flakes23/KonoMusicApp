package com.example.konomusic.client.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.client.ui.adapters.PlaylistAdapter;
import com.example.konomusic.client.viewmodel.PlaylistViewModel;
import com.example.konomusic.shared.model.Playlist;

/**
 * PlaylistFragment - Manage playlists
 * By: KHANG
 */
public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {
    private PlaylistViewModel viewModel;
    private RecyclerView playlistListView;
    private PlaylistAdapter adapter;
    private Button createPlaylistButton;
    private ProgressBar loadingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup button listeners
        setupListeners();

        // Fetch playlists
        viewModel.fetchPlaylists();

        // Observe ViewModel
        observeViewModel();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews(View view) {
        playlistListView = view.findViewById(R.id.playlist_list);
        createPlaylistButton = view.findViewById(R.id.create_playlist_btn);
        loadingBar = view.findViewById(R.id.loading);
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new PlaylistAdapter(this);
        playlistListView.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistListView.setAdapter(adapter);
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        createPlaylistButton.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    /**
     * Show create playlist dialog
     */
    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Playlist");
        builder.setMessage("Enter playlist name:");

        // Setup input
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Setup buttons
        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                viewModel.createPlaylist(playlistName);
            } else {
                Toast.makeText(getContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Observe ViewModel changes
     */
    private void observeViewModel() {
        viewModel.getPlaylistList().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                adapter.setPlaylists(playlists);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handle playlist click
     */
    @Override
    public void onPlaylistClick(Playlist playlist) {
        viewModel.fetchPlaylistTracks(playlist.getId());
        Toast.makeText(getContext(), "Selected: " + playlist.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle playlist delete
     */
    @Override
    public void onPlaylistDelete(Playlist playlist) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete \"" + playlist.getName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                viewModel.deletePlaylist(playlist.getId());
                Toast.makeText(getContext(), "Playlist deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
            .show();
    }
}

