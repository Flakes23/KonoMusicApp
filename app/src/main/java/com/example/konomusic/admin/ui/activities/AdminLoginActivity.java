package com.example.konomusic.admin.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.R;
import com.example.konomusic.shared.utils.Constants;

/**
 * AdminLoginActivity - Màn hình đăng nhập Admin
 */
public class AdminLoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        initViews();
        setupListeners();

        sharedPreferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        editEmail = findViewById(R.id.admin_edit_email);
        editPassword = findViewById(R.id.admin_edit_password);
        btnLogin = findViewById(R.id.admin_btn_login);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginAdmin());
    }

    /**
     * Đăng nhập Admin
     */
    private void loginAdmin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gọi API đăng nhập
        // POST /api/admin/auth/login

        if (email.equals("admin@konomusic.com") && password.equals("admin123")) {
            // Lưu token
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.PREF_TOKEN, "fake_admin_token");
            editor.putString(Constants.PREF_EMAIL, email);
            editor.apply();

            // Navigate to AdminMainActivity
            navigateToMainActivity();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate đến AdminMainActivity
     */
    private void navigateToMainActivity() {
        startActivity(new android.content.Intent(this, AdminMainActivity.class));
        finish();
    }

}

