package com.example.konomusic.client.ui.activities;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.konomusic.R;
import com.example.konomusic.client.ui.fragments.HomeFragment;
import com.example.konomusic.client.ui.fragments.PlaylistFragment;
import com.example.konomusic.client.ui.fragments.ProfileFragment;
import com.example.konomusic.client.ui.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity - Main container activity with BottomNav
 * By: HIẾU
 */
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private FrameLayout fragmentContainer;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);
        fm = getSupportFragmentManager();

        // Show HomeFragment by default
        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }

        // Setup BottomNavigationView listener
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                showFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_playlist) {
                showFragment(new PlaylistFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(new ProfileFragment());
                return true;
            }

            return false;
        });
    }

    /**
     * Show fragment
     */
    private void showFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}

