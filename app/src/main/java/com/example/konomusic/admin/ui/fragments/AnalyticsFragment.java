package com.example.konomusic.admin.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.konomusic.R;

/**
 * AnalyticsFragment - Hiển thị analytics
 */
public class AnalyticsFragment extends Fragment {

    private TextView textTotalVideos;
    private TextView textTotalPlays;
    private TextView textActiveUsers;
    private TextView textTopVideo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadAnalytics();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        textTotalVideos = view.findViewById(R.id.text_total_videos);
        textTotalPlays = view.findViewById(R.id.text_total_plays);
        textActiveUsers = view.findViewById(R.id.text_active_users);
        textTopVideo = view.findViewById(R.id.text_top_video);
    }

    /**
     * Load analytics từ API
     */
    private void loadAnalytics() {
        // TODO: Gọi API GET /api/admin/analytics/videos

        // Sample data
        textTotalVideos.setText("Total Videos: 150");
        textTotalPlays.setText("Total Plays: 50,000");
        textActiveUsers.setText("Active Users: 1,200");
        textTopVideo.setText("Top Video: Never Gonna Give You Up (5,000 plays)");

        Toast.makeText(getContext(), "Analytics loaded", Toast.LENGTH_SHORT).show();
    }

}

