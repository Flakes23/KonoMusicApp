package com.example.konomusic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.R;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.data.firebase.UserProfileSync;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_RESULT = "extra_return_result";

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton registerScreenButton;
    private MaterialButton continueGuestButton;
    private ProgressBar loading;

    private FirebaseAuth auth;
    private boolean returnResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        returnResult = getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false);

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginButton = findViewById(R.id.loginSubmitButton);
        registerScreenButton = findViewById(R.id.loginOpenRegisterButton);
        continueGuestButton = findViewById(R.id.loginContinueGuestButton);
        loading = findViewById(R.id.loginLoading);

        loginButton.setOnClickListener(v -> login());
        registerScreenButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra(RegisterActivity.EXTRA_RETURN_RESULT, returnResult);
            startActivity(intent);
        });
        continueGuestButton.setOnClickListener(v -> openMainForGuest());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            onAuthSuccess(auth.getCurrentUser());
        }
    }

    private void login() {
        String email = valueOf(emailInput);
        String password = valueOf(passwordInput);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.auth_invalid_email));
            emailInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError(getString(R.string.auth_invalid_password));
            passwordInput.requestFocus();
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                String message = AuthErrorMapper.message(
                        LoginActivity.this,
                        task.getException(),
                        R.string.auth_login_failed
                );
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            onAuthSuccess(user);
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        if (user == null) {
            return;
        }

        UserProfileSync.ensureUserDocument(user, "");

        if (returnResult) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openMainForGuest() {
        if (returnResult) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean value) {
        loading.setVisibility(value ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!value);
        registerScreenButton.setEnabled(!value);
        continueGuestButton.setEnabled(!value);
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String firstNonEmpty(String first, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        return fallback;
    }
}
