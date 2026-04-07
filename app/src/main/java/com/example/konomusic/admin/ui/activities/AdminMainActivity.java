package com.example.konomusic.admin.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.konomusic.R;
import com.example.konomusic.UnifiedLoginActivity;
import com.example.konomusic.admin.ui.fragments.AdminHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * AdminMainActivity - Màn hình chính của Admin
 */
public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        try {
            bottomNav = findViewById(R.id.admin_bottom_nav);

            if (savedInstanceState == null) {
                loadFragment(new AdminHomeFragment());
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_admin_home);
                }
            }

            // Setup BottomNavigation listener
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(item -> {
                    androidx.fragment.app.Fragment fragment = null;

                    if (item.getItemId() == R.id.nav_admin_home) {
                        fragment = new AdminHomeFragment();
                    } else if (item.getItemId() == R.id.nav_admin_videos) {
                        Toast.makeText(AdminMainActivity.this, "Videos Management", Toast.LENGTH_SHORT).show();
                    } else if (item.getItemId() == R.id.nav_admin_analytics) {
                        Toast.makeText(AdminMainActivity.this, "Analytics", Toast.LENGTH_SHORT).show();
                    } else if (item.getItemId() == R.id.nav_admin_settings) {
                        Toast.makeText(AdminMainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                    }

                    if (fragment != null) {
                        loadFragment(fragment);
                        return true;
                    }
                    return false;
                });
            }

            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Tải fragment vào container
     */
    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Đăng xuất Admin
     */
    public void logoutAdmin() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        // TODO: Clear session, delete token
        Intent intent = new Intent(this, UnifiedLoginActivity.class);
        startActivity(intent);
        finish();
    }

}

