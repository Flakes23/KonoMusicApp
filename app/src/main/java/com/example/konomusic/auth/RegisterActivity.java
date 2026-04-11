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
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_RESULT = "extra_return_result";

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private MaterialButton registerButton;
    private MaterialButton backToLoginButton;
    private ProgressBar loading;

    private FirebaseAuth auth;
    private boolean returnResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        returnResult = getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false);

        nameInput = findViewById(R.id.registerNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmInput = findViewById(R.id.registerConfirmPasswordInput);
        registerButton = findViewById(R.id.registerSubmitButton);
        backToLoginButton = findViewById(R.id.registerBackLoginButton);
        loading = findViewById(R.id.registerLoading);

        registerButton.setOnClickListener(v -> register());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void register() {
        String displayName = valueOf(nameInput);
        String email = valueOf(emailInput);
        String password = valueOf(passwordInput);
        String confirm = valueOf(confirmInput);

        if (displayName.isEmpty()) {
            nameInput.setError(getString(R.string.auth_invalid_display_name));
            nameInput.requestFocus();
            return;
        }

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

        if (!password.equals(confirm)) {
            confirmInput.setError(getString(R.string.auth_confirm_not_match));
            confirmInput.requestFocus();
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setLoading(false);
                String message = AuthErrorMapper.message(
                        RegisterActivity.this,
                        task.getException(),
                        R.string.auth_register_failed
                );
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                setLoading(false);
                return;
            }

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(updateTask -> {
                setLoading(false);
                UserProfileSync.ensureUserDocument(auth.getCurrentUser(), displayName);
                onRegisterSuccess();
            });
        });
    }

    private void onRegisterSuccess() {
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

    private void setLoading(boolean value) {
        loading.setVisibility(value ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!value);
        backToLoginButton.setEnabled(!value);
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
