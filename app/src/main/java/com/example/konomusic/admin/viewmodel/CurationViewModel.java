package com.example.konomusic.admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * CurationViewModel - ViewModel cho Curation
 */
public class CurationViewModel extends ViewModel {

    private MutableLiveData<java.util.List<String>> curations;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public CurationViewModel() {
        curations = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    /**
     * Lấy danh sách curation
     */
    public LiveData<java.util.List<String>> getCurations() {
        if (curations.getValue() == null) {
            loadCurations();
        }
        return curations;
    }

    /**
     * Load curations từ API
     */
    private void loadCurations() {
        isLoading.setValue(true);

        // TODO: GET /api/admin/curation/pending

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("Video 1 - Pending");
        list.add("Video 2 - Pending");

        curations.setValue(list);
        isLoading.setValue(false);
    }

    /**
     * Phê duyệt curation
     */
    public void approveCuration(Long curationId) {
        // TODO: POST /api/admin/curation/{id}/approve
    }

    /**
     * Từ chối curation
     */
    public void rejectCuration(Long curationId, String reason) {
        // TODO: POST /api/admin/curation/{id}/reject
    }

    /**
     * Lấy status loading
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

