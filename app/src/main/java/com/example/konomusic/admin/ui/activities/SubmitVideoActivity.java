package com.example.konomusic.admin.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konomusic.R;

/**
 * SubmitVideoActivity - Màn hình submit video để curation
 */
public class SubmitVideoActivity extends AppCompatActivity {

    private EditText editYoutubeId;
    private EditText editTitle;
    private EditText editChannelName;
    private EditText editNote;
    private Spinner spinnerGenre;
    private Button btnSubmit;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_video);

        initViews();
        setupListeners();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        editYoutubeId = findViewById(R.id.edit_youtube_id);
        editTitle = findViewById(R.id.edit_title);
        editChannelName = findViewById(R.id.edit_channel_name);
        editNote = findViewById(R.id.edit_note);
        spinnerGenre = findViewById(R.id.spinner_genre);
        btnSubmit = findViewById(R.id.btn_submit_video);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitVideo());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Submit video để curation
     */
    private void submitVideo() {
        String youtubeId = editYoutubeId.getText().toString().trim();
        String title = editTitle.getText().toString().trim();
        String channelName = editChannelName.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (youtubeId.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gọi API submit video
        // POST /api/admin/curation/submit

        Toast.makeText(this, "Video submitted for curation!", Toast.LENGTH_SHORT).show();
        finish();
    }

}

