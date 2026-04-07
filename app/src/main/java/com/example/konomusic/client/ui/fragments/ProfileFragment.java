package com.example.konomusic.client.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.UnifiedLoginActivity;
import com.example.konomusic.client.viewmodel.ProfileViewModel;
import com.example.konomusic.shared.model.User;
import com.example.konomusic.shared.model.UserStatistics;

/**
 * ProfileFragment - User profile display
 * By: KHƯƠNG
 */
public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private ImageView avatar;
    private TextView userName;
    private TextView userEmail;
    private TextView playlistCount;
    private TextView playCount;
    private Button editButton;
    private Button logoutButton;
    private ProgressBar loadingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Setup listeners
        setupListeners();

        // Fetch data
        viewModel.fetchUserInfo();
        viewModel.fetchStatistics();

        // Observe ViewModel
        observeViewModel();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews(View view) {
        avatar = view.findViewById(R.id.avatar);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        playlistCount = view.findViewById(R.id.playlist_count);
        playCount = view.findViewById(R.id.play_count);
        editButton = view.findViewById(R.id.edit_button);
        logoutButton = view.findViewById(R.id.logout_button);
        loadingBar = view.findViewById(R.id.loading);
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        editButton.setOnClickListener(v -> handleEditProfile());
        logoutButton.setOnClickListener(v -> handleLogout());
    }

    /**
     * Handle edit profile
     */
    private void handleEditProfile() {
        // TODO: Navigate to edit profile screen
    }

    /**
     * Handle logout
     */
    private void handleLogout() {
        viewModel.logout();

        Intent intent = new Intent(getActivity(), UnifiedLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Observe ViewModel changes
     */
    private void observeViewModel() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                displayUserInfo(user);
            }
        });

        viewModel.getStatistics().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                displayStatistics(stats);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Display user information
     */
    private void displayUserInfo(User user) {
        userName.setText(user.getDisplayName());
        userEmail.setText(user.getEmail());

        // Load avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(avatar);
        }
    }

    /**
     * Display statistics
     */
    private void displayStatistics(UserStatistics stats) {
        playlistCount.setText(String.valueOf(stats.getTotalPlaylists()));
        playCount.setText(String.valueOf(stats.getTotalPlays()));
    }
}

