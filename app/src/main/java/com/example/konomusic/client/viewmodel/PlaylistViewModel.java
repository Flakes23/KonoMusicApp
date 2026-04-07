package com.example.konomusic.client.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.shared.model.Playlist;
import com.example.konomusic.shared.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * PlaylistViewModel - Handle playlist CRUD operations
 * By: KHANG
 */
public class PlaylistViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Playlist>> playlistList = new MutableLiveData<>();
    private final MutableLiveData<Playlist> selectedPlaylist = new MutableLiveData<>();
    private final MutableLiveData<List<Video>> playlistTracks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Fetch all playlists
     */
    public void fetchPlaylists() {
        isLoading.setValue(true);

        // TODO: Call API service
        // For now, mock data
        try {
            Thread.sleep(500);

            List<Playlist> playlists = generateMockPlaylists();
            playlistList.setValue(playlists);
            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Create new playlist
     */
    public void createPlaylist(String name) {
        isLoading.setValue(true);

        // TODO: Call API service
        try {
            Thread.sleep(500);

            Playlist newPlaylist = new Playlist(
                System.currentTimeMillis(),
                name
            );

            List<Playlist> current = playlistList.getValue();
            if (current != null) {
                current.add(newPlaylist);
                playlistList.setValue(new ArrayList<>(current));
            }

            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Create playlist error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Delete playlist
     */
    public void deletePlaylist(Long id) {
        // TODO: Call API service
        List<Playlist> current = playlistList.getValue();
        if (current != null) {
            current.removeIf(p -> p.getId().equals(id));
            playlistList.setValue(new ArrayList<>(current));
        }
    }

    /**
     * Add video to playlist
     */
    public void addVideoToPlaylist(Long playlistId, Long videoId) {
        // TODO: Call API service
        errorMessage.setValue(null);
    }

    /**
     * Remove video from playlist
     */
    public void removeVideoFromPlaylist(Long playlistId, Long videoId) {
        // TODO: Call API service
        errorMessage.setValue(null);
    }

    /**
     * Fetch playlist tracks
     */
    public void fetchPlaylistTracks(Long playlistId) {
        // TODO: Call API service
        List<Video> tracks = new ArrayList<>();
        playlistTracks.setValue(tracks);
    }

    /**
     * Generate mock playlists
     */
    private List<Playlist> generateMockPlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Playlist p = new Playlist((long)i, "My Playlist " + i);
            playlists.add(p);
        }

        return playlists;
    }

    // Getters
    public LiveData<List<Playlist>> getPlaylistList() { return playlistList; }
    public LiveData<Playlist> getSelectedPlaylist() { return selectedPlaylist; }
    public LiveData<List<Video>> getPlaylistTracks() { return playlistTracks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

