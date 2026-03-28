package com.example.konomusic.admin.ui.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.R;

/**
 * ViewAnalyticsActivity - Màn hình xem analytics
 */
public class ViewAnalyticsActivity extends AppCompatActivity {

    private Spinner spinnerPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_analytics);

        initViews();
        setupSpinner();
        loadAnalytics();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        spinnerPeriod = findViewById(R.id.spinner_analytics_period);
    }

    /**
     * Setup spinner cho period
     */
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_periods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);

        spinnerPeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                loadAnalytics();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    /**
     * Load analytics từ API
     */
    private void loadAnalytics() {
        String period = spinnerPeriod.getSelectedItem().toString();

        // TODO: Gọi API lấy analytics
        // GET /api/admin/analytics/videos?period=7d
        // GET /api/admin/analytics/top-videos?limit=10&period=30d

        Toast.makeText(this, "Loading analytics for " + period + "...", Toast.LENGTH_SHORT).show();
    }

}

