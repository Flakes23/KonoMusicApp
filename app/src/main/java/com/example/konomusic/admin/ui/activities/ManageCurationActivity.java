package com.example.konomusic.admin.ui.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.R;

/**
 * ManageCurationActivity - Màn hình quản lý curation queue
 */
public class ManageCurationActivity extends AppCompatActivity {

    private ListView listViewCuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_curation);

        initViews();
        loadCurationQueue();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        listViewCuration = findViewById(R.id.list_curation_items);
    }

    /**
     * Load curation queue từ API
     */
    private void loadCurationQueue() {
        // TODO: Gọi API lấy pending curations
        // GET /api/admin/curation/pending

        Toast.makeText(this, "Loading curation queue...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Phê duyệt video
     */
    public void approveVideo(Long curationId) {
        // TODO: Gọi API phê duyệt
        // POST /api/admin/curation/{id}/approve

        Toast.makeText(this, "Video approved!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Từ chối video
     */
    public void rejectVideo(Long curationId) {
        // TODO: Gọi API từ chối
        // POST /api/admin/curation/{id}/reject

        Toast.makeText(this, "Video rejected!", Toast.LENGTH_SHORT).show();
    }

}

