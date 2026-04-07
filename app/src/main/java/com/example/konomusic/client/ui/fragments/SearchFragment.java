package com.example.konomusic.client.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.client.ui.activities.PlayerActivity;
import com.example.konomusic.client.ui.adapters.VideoAdapter;
import com.example.konomusic.client.viewmodel.SearchViewModel;
import com.example.konomusic.shared.model.Video;

/**
 * SearchFragment - Search for videos
 * By: KHANG
 */
public class SearchFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private SearchViewModel viewModel;
    private SearchView searchView;
    private RecyclerView searchResultsView;
    private VideoAdapter adapter;
    private ProgressBar loadingBar;
    private TextView emptyState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup SearchView
        setupSearchView();

        // Observe ViewModel
        observeViewModel();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews(View view) {
        searchView = view.findViewById(R.id.search_view);
        searchResultsView = view.findViewById(R.id.search_results);
        loadingBar = view.findViewById(R.id.loading);
        emptyState = view.findViewById(R.id.empty_state);
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new VideoAdapter(this);
        searchResultsView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsView.setAdapter(adapter);
    }

    /**
     * Setup SearchView listener
     */
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.search(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.search(query);
                return true;
            }
        });
    }

    /**
     * Observe ViewModel changes
     */
    private void observeViewModel() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                adapter.setVideos(results);
                searchResultsView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                searchResultsView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                emptyState.setText("No results found");
            }
        });

        viewModel.getIsSearching().observe(getViewLifecycleOwner(), isSearching -> {
            loadingBar.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                emptyState.setText(error);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Handle video item click
     */
    @Override
    public void onVideoClick(Video video) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("video", video);
        startActivity(intent);
    }
}

