package com.example.konomusic.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class AuthManager {

    public static final int LOGIN_REQUEST_CODE = 1101;

    private AuthManager() {
    }

    public static boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static FirebaseUser currentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void openLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    public static void openLoginForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_RETURN_RESULT, true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}

