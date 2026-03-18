package com.example.konomusic.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.konomusic.R;

public class AdminSectionFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_ICON  = "icon";

    public static AdminSectionFragment newInstance(String title, int iconResId) {
        AdminSectionFragment f = new AdminSectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_ICON, iconResId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            TextView tvTitle = view.findViewById(R.id.tv_section_title);
            TextView tvSub   = view.findViewById(R.id.tv_section_sub);
            ImageView ivIcon = view.findViewById(R.id.iv_section_icon);

            String title = getArguments().getString(ARG_TITLE, "");
            int iconRes  = getArguments().getInt(ARG_ICON, R.drawable.ic_music_note);

            tvTitle.setText("Quản lý " + title);
            tvSub.setText("Chức năng đang được phát triển...");
            ivIcon.setImageResource(iconRes);
        }
    }
}

