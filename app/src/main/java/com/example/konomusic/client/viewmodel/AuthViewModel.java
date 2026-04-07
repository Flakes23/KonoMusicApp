package com.example.konomusic.client.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.client.utils.ClientConstants;

/**
 * AuthViewModel - Handle login/register logic
 * By: TẤN
 */
public class AuthViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.sharedPreferences = application.getSharedPreferences(
            ClientConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Login with email and password
     */
    public void login(String email, String password) {
        isLoading.setValue(true);

        // TODO: Call API service
        // For now, mock success
        try {
            Thread.sleep(1000); // Simulate network delay

            // Mock successful login
            saveToken("mock_jwt_token_" + System.currentTimeMillis());
            saveUserId(1);
            saveEmail(email);

            loginSuccess.setValue(true);
            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Login failed: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Register new account
     */
    public void register(String email, String name, String password) {
        isLoading.setValue(true);

        // TODO: Call API service
        // For now, mock success
        try {
            Thread.sleep(1000); // Simulate network delay

            registerSuccess.setValue(true);
            isLoading.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Registration failed: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Logout
     */
    public void logout() {
        sharedPreferences.edit().clear().apply();
        loginSuccess.setValue(false);
    }

    /**
     * Save token to SharedPreferences
     */
    private void saveToken(String token) {
        sharedPreferences.edit()
            .putString(ClientConstants.KEY_TOKEN, token)
            .apply();
    }

    /**
     * Get token from SharedPreferences
     */
    public String getToken() {
        return sharedPreferences.getString(ClientConstants.KEY_TOKEN, "");
    }

    /**
     * Save user ID
     */
    private void saveUserId(int userId) {
        sharedPreferences.edit()
            .putInt(ClientConstants.KEY_USER_ID, userId)
            .apply();
    }

    /**
     * Save email
     */
    private void saveEmail(String email) {
        sharedPreferences.edit()
            .putString(ClientConstants.KEY_EMAIL, email)
            .apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return !getToken().isEmpty();
    }

    // Getters for LiveData
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getRegisterSuccess() { return registerSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

