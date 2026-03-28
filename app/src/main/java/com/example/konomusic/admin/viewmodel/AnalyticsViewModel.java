package com.example.konomusic.admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * AnalyticsViewModel - ViewModel cho Analytics
 */
public class AnalyticsViewModel extends ViewModel {

    private MutableLiveData<Long> totalVideos;
    private MutableLiveData<Long> totalPlays;
    private MutableLiveData<Long> activeUsers;
    private MutableLiveData<String> topVideo;
    private MutableLiveData<Boolean> isLoading;

    public AnalyticsViewModel() {
        totalVideos = new MutableLiveData<>();
        totalPlays = new MutableLiveData<>();
        activeUsers = new MutableLiveData<>();
        topVideo = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
    }

    /**
     * Lấy tổng videos
     */
    public LiveData<Long> getTotalVideos() {
        if (totalVideos.getValue() == null) {
            loadAnalytics();
        }
        return totalVideos;
    }

    /**
     * Lấy tổng plays
     */
    public LiveData<Long> getTotalPlays() {
        if (totalPlays.getValue() == null) {
            loadAnalytics();
        }
        return totalPlays;
    }

    /**
     * Lấy active users
     */
    public LiveData<Long> getActiveUsers() {
        if (activeUsers.getValue() == null) {
            loadAnalytics();
        }
        return activeUsers;
    }

    /**
     * Lấy top video
     */
    public LiveData<String> getTopVideo() {
        if (topVideo.getValue() == null) {
            loadAnalytics();
        }
        return topVideo;
    }

    /**
     * Load analytics từ API
     */
    private void loadAnalytics() {
        isLoading.setValue(true);

        // TODO: GET /api/admin/analytics/videos

        totalVideos.setValue(150L);
        totalPlays.setValue(50000L);
        activeUsers.setValue(1200L);
        topVideo.setValue("Never Gonna Give You Up - 5000 plays");

        isLoading.setValue(false);
    }

    /**
     * Lấy status loading
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Refresh analytics
     */
    public void refreshAnalytics() {
        loadAnalytics();
    }

}

