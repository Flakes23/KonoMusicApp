package com.example.konomusic.client.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.shared.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeViewModel - Fetch videos
 * By: HIẾU
 */
public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Video>> videoList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Video> selectedVideo = new MutableLiveData<>();

    private int currentPage = 1;
    private List<Video> allVideos = new ArrayList<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Fetch videos from API
     */
    public void fetchVideos(int page) {
        isLoading.setValue(true);
        currentPage = page;

        // TODO: Call API service
        // For now, mock data
        try {
            Thread.sleep(500);

            List<Video> mockVideos = generateMockVideos(page);
            if (page == 1) {
                allVideos = new ArrayList<>(mockVideos);
            } else {
                allVideos.addAll(mockVideos);
            }

            videoList.setValue(allVideos);
            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Refresh videos
     */
    public void refreshVideos() {
        fetchVideos(1);
    }

    /**
     * Load next page
     */
    public void loadNextPage() {
        fetchVideos(currentPage + 1);
    }

    /**
     * Select video
     */
    public void selectVideo(Video video) {
        selectedVideo.setValue(video);
    }

    /**
     * Generate mock videos for testing
     */
    private List<Video> generateMockVideos(int page) {
        List<Video> videos = new ArrayList<>();
        int startId = (page - 1) * 10 + 1;

        for (int i = 0; i < 10; i++) {
            long id = startId + i;
            videos.add(new Video(
                id,
                "youtubeId" + id,
                "Song Title " + id,
                "Artist " + (id % 5 + 1),
                180 + (i * 30),
                (long) (1000 + i * 100),
                "https://via.placeholder.com/200"
            ));
        }

        return videos;
    }

    // Getters
    public LiveData<List<Video>> getVideoList() { return videoList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Video> getSelectedVideo() { return selectedVideo; }
}

