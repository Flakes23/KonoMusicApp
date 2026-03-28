package com.example.konomusic.admin.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.konomusic.R;
import com.example.konomusic.admin.ui.activities.ManageCurationActivity;
import com.example.konomusic.admin.ui.activities.SubmitVideoActivity;
import com.example.konomusic.admin.ui.activities.ViewAnalyticsActivity;

/**
 * AdminHomeFragment - Màn hình chính Admin
 */
public class AdminHomeFragment extends Fragment {

    private LinearLayout cardSubmitVideo;
    private LinearLayout cardManageCuration;
    private LinearLayout cardAnalytics;
    private Button btnLogout;
    private TextView textAdminWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        displayAdminInfo();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        cardSubmitVideo = view.findViewById(R.id.card_submit_video);
        cardManageCuration = view.findViewById(R.id.card_manage_curation);
        cardAnalytics = view.findViewById(R.id.card_analytics);
        btnLogout = view.findViewById(R.id.btn_admin_logout);
        textAdminWelcome = view.findViewById(R.id.text_admin_welcome);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        cardSubmitVideo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SubmitVideoActivity.class);
            startActivity(intent);
        });

        cardManageCuration.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageCurationActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewAnalyticsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutAdmin());
    }

    /**
     * Hiển thị thông tin Admin
     */
    private void displayAdminInfo() {
        String adminEmail = "admin@konomusic.com"; // TODO: Get from SharedPreferences
        textAdminWelcome.setText("Welcome back!");
    }

    /**
     * Đăng xuất Admin
     */
    private void logoutAdmin() {
        // TODO: Clear session
        if (getActivity() instanceof com.example.konomusic.admin.ui.activities.AdminMainActivity) {
            ((com.example.konomusic.admin.ui.activities.AdminMainActivity) getActivity()).logoutAdmin();
        }
    }

}

