package com.example.konomusic.client.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.konomusic.R;
import com.example.konomusic.client.ui.activities.PlayerActivity;
import com.example.konomusic.client.ui.adapters.VideoAdapter;
import com.example.konomusic.client.viewmodel.HomeViewModel;
import com.example.konomusic.shared.model.Video;

/**
 * HomeFragment - Display video list
 * By: HIẾU
 */
public class HomeFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private HomeViewModel viewModel;
    private RecyclerView videoListView;
    private VideoAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar loadingBar;
    private TextView emptyState;
    private boolean isLoadingMore = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup SwipeRefresh
        setupSwipeRefresh();

        // Fetch videos
        viewModel.fetchVideos(1);

        // Observe ViewModel
        observeViewModel();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews(View view) {
        videoListView = view.findViewById(R.id.video_list);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingBar = view.findViewById(R.id.loading);
        emptyState = view.findViewById(R.id.empty_state);
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new VideoAdapter(this);
        videoListView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoListView.setAdapter(adapter);

        // Add scroll listener for pagination
        videoListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                // Load more when near the end
                if (!isLoadingMore && lastVisibleItem >= totalItemCount - 5) {
                    isLoadingMore = true;
                    viewModel.loadNextPage();
                }
            }
        });
    }

    /**
     * Setup SwipeRefresh
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshVideos();
            swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * Observe ViewModel changes
     */
    private void observeViewModel() {
        viewModel.getVideoList().observe(getViewLifecycleOwner(), videos -> {
            if (videos != null && !videos.isEmpty()) {
                adapter.setVideos(videos);
                emptyState.setVisibility(View.GONE);
                isLoadingMore = false;
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
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
        viewModel.selectVideo(video);

        // Navigate to PlayerActivity
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("video", video);
        startActivity(intent);
    }
}

