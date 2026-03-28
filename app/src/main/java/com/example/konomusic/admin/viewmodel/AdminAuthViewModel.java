package com.example.konomusic.admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * AdminAuthViewModel - ViewModel cho Admin Authentication
 */
public class AdminAuthViewModel extends ViewModel {

    private MutableLiveData<String> authToken;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoggedIn;

    public AdminAuthViewModel() {
        authToken = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        isLoggedIn = new MutableLiveData<>();
    }

    /**
     * Login admin
     */
    public void login(String email, String password) {
        isLoading.setValue(true);

        // TODO: POST /api/admin/auth/login

        if (email.equals("admin@konomusic.com") && password.equals("admin123")) {
            authToken.setValue("fake_admin_token");
            isLoggedIn.setValue(true);
            errorMessage.setValue(null);
        } else {
            errorMessage.setValue("Invalid email or password");
            isLoggedIn.setValue(false);
        }

        isLoading.setValue(false);
    }

    /**
     * Logout admin
     */
    public void logout() {
        authToken.setValue(null);
        isLoggedIn.setValue(false);
        errorMessage.setValue(null);
    }

    /**
     * Lấy auth token
     */
    public LiveData<String> getAuthToken() {
        return authToken;
    }

    /**
     * Lấy login status
     */
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Lấy loading status
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Lấy error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

}

