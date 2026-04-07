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

/**
 * UnifiedRegisterActivity - Đăng ký chung cho Admin và User
 */
public class UnifiedRegisterActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword, inputConfirmPassword, inputName;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        inputName = findViewById(R.id.input_name);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        // Register button
        btnRegister.setOnClickListener(v -> handleRegister());

        // Back to login link
        findViewById(R.id.link_back_login).setOnClickListener(v -> {
            finish();
        });
    }

    private void handleRegister() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();
        String name = inputName.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Auto-detect: Email chứa "admin" thì register as admin (không cho phép)
        boolean isAdmin = email.toLowerCase().contains("admin");

        if (isAdmin) {
            Toast.makeText(this, "Không thể đăng ký tài khoản admin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register as user
        registerAsUser(email, password, name);
    }

    private void registerAsUser(String email, String password, String name) {
        // Save to SharedPreferences (demo - không có backend)
        SharedPreferences prefs = getSharedPreferences("KonoMusic_Client", MODE_PRIVATE);
        prefs.edit()
            .putString("user_email", email)
            .putString("user_name", name)
            .putString("user_password", password)
            .putBoolean("is_logged_in", true)
            .apply();

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

        // Go to Client Dashboard
        Intent intent = new Intent(UnifiedRegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

