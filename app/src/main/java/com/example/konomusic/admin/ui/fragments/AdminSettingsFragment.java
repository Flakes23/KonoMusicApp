package com.example.konomusic.admin.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.konomusic.R;
import com.example.konomusic.shared.utils.Constants;

/**
 * AdminSettingsFragment - Cài đặt Admin
 */
public class AdminSettingsFragment extends Fragment {

    private TextView textAdminEmail;
    private Button btnChangePassword;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
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
        textAdminEmail = view.findViewById(R.id.text_admin_email);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_settings_logout);

        sharedPreferences = getContext().getSharedPreferences(Constants.PREF_NAME, 0);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnLogout.setOnClickListener(v -> logoutAdmin());
    }

    /**
     * Hiển thị thông tin Admin
     */
    private void displayAdminInfo() {
        String email = sharedPreferences.getString(Constants.PREF_EMAIL, "admin@konomusic.com");
        textAdminEmail.setText("Email: " + email);
    }

    /**
     * Đổi mật khẩu
     */
    private void changePassword() {
        // TODO: Mở dialog đổi mật khẩu
        Toast.makeText(getContext(), "Change password feature coming soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Đăng xuất
     */
    private void logoutAdmin() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

        // TODO: Navigate to AdminLoginActivity
    }

}

