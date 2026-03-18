package com.example.konomusic.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.konomusic.R;
import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                android.R.string.ok, android.R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardHomeFragment(), "KonoMusic Admin");
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String title = item.getTitle().toString();

        if (id == R.id.nav_dashboard) {
            loadFragment(new DashboardHomeFragment(), "KonoMusic Admin");
        } else if (id == R.id.nav_songs) {
            loadFragment(AdminSectionFragment.newInstance("Bài hát", R.drawable.ic_music_note), title);
        } else if (id == R.id.nav_artists) {
            loadFragment(AdminSectionFragment.newInstance("Nghệ sĩ", R.drawable.ic_artist), title);
        } else if (id == R.id.nav_albums) {
            loadFragment(AdminSectionFragment.newInstance("Album", R.drawable.ic_album), title);
        } else if (id == R.id.nav_users) {
            loadFragment(AdminSectionFragment.newInstance("Người dùng", R.drawable.ic_people), title);
        } else if (id == R.id.nav_settings) {
            loadFragment(AdminSectionFragment.newInstance("Cài đặt", R.drawable.ic_settings), title);
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}

