package com.example.konomusic.admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.konomusic.shared.model.User;

/**
 * AdminDashboardViewModel - ViewModel cho Admin Dashboard
 */
public class AdminDashboardViewModel extends ViewModel {

    private MutableLiveData<User> adminUser;
    private MutableLiveData<Integer> pendingCurations;
    private MutableLiveData<Long> totalVideos;
    private MutableLiveData<Long> totalPlays;

    public AdminDashboardViewModel() {
        adminUser = new MutableLiveData<>();
        pendingCurations = new MutableLiveData<>();
        totalVideos = new MutableLiveData<>();
        totalPlays = new MutableLiveData<>();
    }

    /**
     * Lấy thông tin Admin
     */
    public LiveData<User> getAdminUser() {
        if (adminUser.getValue() == null) {
            loadAdminUser();
        }
        return adminUser;
    }

    /**
     * Load Admin user từ API
     */
    private void loadAdminUser() {
        // TODO: GET /api/admin/user-info
        User user = new User();
        user.setEmail("admin@konomusic.com");
        user.setDisplayName("Administrator");
        adminUser.setValue(user);
    }

    /**
     * Lấy số curation đang chờ
     */
    public LiveData<Integer> getPendingCurations() {
        if (pendingCurations.getValue() == null) {
            loadPendingCurations();
        }
        return pendingCurations;
    }

    /**
     * Load pending curations
     */
    private void loadPendingCurations() {
        // TODO: GET /api/admin/curation/pending
        pendingCurations.setValue(5);
    }

    /**
     * Lấy tổng số videos
     */
    public LiveData<Long> getTotalVideos() {
        if (totalVideos.getValue() == null) {
            loadTotalVideos();
        }
        return totalVideos;
    }

    /**
     * Load total videos
     */
    private void loadTotalVideos() {
        // TODO: GET /api/analytics/total-videos
        totalVideos.setValue(150L);
    }

    /**
     * Lấy tổng số plays
     */
    public LiveData<Long> getTotalPlays() {
        if (totalPlays.getValue() == null) {
            loadTotalPlays();
        }
        return totalPlays;
    }

    /**
     * Load total plays
     */
    private void loadTotalPlays() {
        // TODO: GET /api/analytics/total-plays
        totalPlays.setValue(50000L);
    }

}

