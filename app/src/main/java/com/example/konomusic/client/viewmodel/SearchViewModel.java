package com.example.konomusic.client.viewmodel;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.client.utils.ClientConstants;
import com.example.konomusic.shared.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchViewModel - Handle search logic with debounce
 * By: KHANG
 */
public class SearchViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Video>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);
    private final MutableLiveData<String> query = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final Handler handler = new Handler();
    private Runnable searchRunnable;

    public SearchViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Search videos with debounce
     */
    public void search(String keyword) {
        query.setValue(keyword);

        // Remove previous search runnable
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        if (keyword.isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        // Debounce - wait 500ms before searching
        searchRunnable = () -> performSearch(keyword);
        handler.postDelayed(searchRunnable, ClientConstants.DEBOUNCE_DELAY_MS);
    }

    /**
     * Perform actual search
     */
    private void performSearch(String keyword) {
        isSearching.setValue(true);

        // TODO: Call API service
        // For now, mock data
        try {
            Thread.sleep(500);

            List<Video> results = generateMockSearchResults(keyword);
            searchResults.setValue(results);
            isSearching.setValue(false);
        } catch (InterruptedException e) {
            errorMessage.setValue("Search error: " + e.getMessage());
            isSearching.setValue(false);
        }
    }

    /**
     * Clear search
     */
    public void clearSearch() {
        query.setValue("");
        searchResults.setValue(new ArrayList<>());
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
    }

    /**
     * Generate mock search results
     */
    private List<Video> generateMockSearchResults(String keyword) {
        List<Video> results = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            long id = i + 1;
            results.add(new Video(
                id,
                "youtubeId" + id,
                "Search Result: " + keyword + " " + i,
                "Artist " + (i + 1),
                180 + (i * 30),
                (long) (1000 + i * 100),
                "https://via.placeholder.com/200"
            ));
        }

        return results;
    }

    // Getters
    public LiveData<List<Video>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> getIsSearching() { return isSearching; }
    public LiveData<String> getQuery() { return query; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

