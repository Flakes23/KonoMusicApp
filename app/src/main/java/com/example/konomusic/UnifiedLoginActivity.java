package com.example.konomusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.admin.ui.activities.AdminMainActivity;
import com.example.konomusic.client.ui.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * UnifiedLoginActivity - Đăng nhập chung cho Admin và User
 * Tự động xác định tài khoản là admin hay user dựa trên email
 */
public class UnifiedLoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void setupListeners() {
        // Nút đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Link đăng ký - chỉ cho user
        findViewById(R.id.link_register).setOnClickListener(v -> {
            Intent intent = new Intent(UnifiedLoginActivity.this, UnifiedRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tự động nhận biết: Email chứa "admin" thì đăng nhập as admin, không thì đăng nhập as user
        boolean isAdmin = email.toLowerCase().contains("admin");

        if (isAdmin) {
            // Demo: admin@kono.com / admin123
            if (email.equals("admin@kono.com") && password.equals("admin123")) {
                loginAsAdmin(email);
            } else {
                Toast.makeText(this, "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Demo: user@kono.com / 123456 hoặc bất kỳ email không chứa "admin"
            if (email.equals("user@kono.com") && password.equals("123456")) {
                loginAsUser(email);
            } else {
                Toast.makeText(this, "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginAsAdmin(String email) {
        // Lưu vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences("KonoMusic_Admin", MODE_PRIVATE);
        prefs.edit()
            .putString("admin_email", email)
            .putBoolean("is_logged_in", true)
            .apply();

        Toast.makeText(this, "Đăng nhập admin thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển đến Admin Dashboard
        Intent intent = new Intent(UnifiedLoginActivity.this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginAsUser(String email) {
        // Lưu vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences("KonoMusic_Client", MODE_PRIVATE);
        prefs.edit()
            .putString("user_email", email)
            .putBoolean("is_logged_in", true)
            .apply();

        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển đến Client Dashboard
        Intent intent = new Intent(UnifiedLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

