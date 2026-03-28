package com.example.konomusic.admin.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.konomusic.R;
import com.example.konomusic.admin.ui.fragments.AdminHomeFragment;

/**
 * AdminMainActivity - Màn hình chính của Admin
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment());
        }

        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Tải fragment vào container
     */
    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Đăng xuất Admin
     */
    public void logoutAdmin() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        // TODO: Clear session, delete token
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        finish();
    }

}

