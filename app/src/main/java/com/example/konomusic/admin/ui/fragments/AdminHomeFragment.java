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

    private Button btnUploadVideo;
    private Button btnManageContent;
    private Button btnViewReports;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            initViews(view);
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        try {
            btnUploadVideo = view.findViewById(R.id.btn_upload_video);
            btnManageContent = view.findViewById(R.id.btn_manage_content);
            btnViewReports = view.findViewById(R.id.btn_view_reports);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        try {
            if (btnUploadVideo != null) {
                btnUploadVideo.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), SubmitVideoActivity.class);
                    startActivity(intent);
                });
            }

            if (btnManageContent != null) {
                btnManageContent.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ManageCurationActivity.class);
                    startActivity(intent);
                });
            }

            if (btnViewReports != null) {
                btnViewReports.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewAnalyticsActivity.class);
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

