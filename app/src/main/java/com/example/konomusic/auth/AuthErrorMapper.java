package com.example.konomusic.auth;

import android.content.Context;

import com.example.konomusic.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthException;

public final class AuthErrorMapper {

    private AuthErrorMapper() {
    }

    public static String message(Context context, Exception error, int fallbackRes) {
        if (context == null) {
            return "Authentication error";
        }

        if (error == null) {
            return context.getString(fallbackRes);
        }

        String raw = error.getMessage() == null ? "" : error.getMessage();

        if (raw.toUpperCase().contains("CONFIGURATION_NOT_FOUND")) {
            return context.getString(R.string.auth_configuration_not_found);
        }

        if (error instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) error).getErrorCode();
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                return context.getString(R.string.auth_email_in_use);
            }
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return context.getString(R.string.auth_invalid_email);
            }
            if ("ERROR_WEAK_PASSWORD".equals(code)) {
                return context.getString(R.string.auth_invalid_password);
            }
            if ("ERROR_USER_NOT_FOUND".equals(code) || "ERROR_WRONG_PASSWORD".equals(code)) {
                return context.getString(R.string.auth_invalid_credentials);
            }
        }

        if (error instanceof FirebaseException && !raw.trim().isEmpty()) {
            return raw.trim();
        }

        return context.getString(fallbackRes);
    }
}

