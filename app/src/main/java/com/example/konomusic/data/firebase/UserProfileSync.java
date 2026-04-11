package com.example.konomusic.data.firebase;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserProfileSync {

    private UserProfileSync() {
    }

    public static void ensureUserDocument(FirebaseUser user, String fallbackDisplayName) {
        if (user == null) {
            return;
        }

        String displayName = firstNonEmpty(user.getDisplayName(), fallbackDisplayName, "Kono User");
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        List<String> providers = new ArrayList<>();
        if (user.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
                if (info != null && info.getProviderId() != null && !info.getProviderId().trim().isEmpty()) {
                    providers.add(info.getProviderId().trim());
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("email", firstNonEmpty(user.getEmail(), ""));
        data.put("displayName", displayName);
        data.put("avatarUrl", photoUrl);
        data.put("providers", providers);
        data.put("isGuest", false);
        data.put("lastLoginAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && !snapshot.exists()) {
                data.put("createdAt", FieldValue.serverTimestamp());
            }
            db.collection("users").document(user.getUid()).set(data, SetOptions.merge());
        });
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}

