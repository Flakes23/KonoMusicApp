package com.example.konomusic.client.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.client.utils.ClientConstants;
import com.example.konomusic.shared.model.User;
import com.example.konomusic.shared.model.UserStatistics;
import com.example.konomusic.shared.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * ProfileViewModel - User profile and statistics
 * By: KHƯƠNG
 */
public class ProfileViewModel extends AndroidViewModel {
    private final MutableLiveData<User> userInfo = new MutableLiveData<>();
    private final MutableLiveData<UserStatistics> statistics = new MutableLiveData<>();
    private final MutableLiveData<List<Video>> favoriteVideos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.sharedPreferences = application.getSharedPreferences(
            ClientConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Fetch user information
     */
    public void fetchUserInfo() {
        isLoading.setValue(true);

        // TODO: Call API service
        // For now, mock data
        try {
            Thread.sleep(500);

            String email = sharedPreferences.getString(ClientConstants.KEY_EMAIL, "user@example.com");
            User user = new User(
                1L,
                email,
                "Kono User"
            );

            userInfo.setValue(user);
            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Fetch user statistics
     */
    public void fetchStatistics() {
        // TODO: Call API service
        // For now, mock data
        UserStatistics stats = new UserStatistics(5, 3600000, 150, 25);
        statistics.setValue(stats);
    }

    /**
     * Fetch favorite videos
     */
    public void fetchFavoriteVideos() {
        // TODO: Call API service
        List<Video> favorites = new ArrayList<>();
        favoriteVideos.setValue(favorites);
    }

    /**
     * Logout
     */
    public void logout() {
        sharedPreferences.edit().clear().apply();
    }

    // Getters
    public LiveData<User> getUserInfo() { return userInfo; }
    public LiveData<UserStatistics> getStatistics() { return statistics; }
    public LiveData<List<Video>> getFavoriteVideos() { return favoriteVideos; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

