package com.example.konomusic.client.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.client.viewmodel.PlayerViewModel;
import com.example.konomusic.shared.model.Video;

/**
 * PlayerActivity - Music player screen
 * By: HUY
 */
public class PlayerActivity extends AppCompatActivity {
    private ImageView thumbnail;
    private TextView titleText;
    private TextView artistText;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private SeekBar seekBar;
    private Button playButton;
    private Button prevButton;
    private Button nextButton;
    private Button likeButton;
    private Button playlistButton;
    private Button shareButton;

    private PlayerViewModel viewModel;
    private Video currentVideo;
    private boolean isSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get video from intent
        currentVideo = getIntent().getParcelableExtra("video");
        if (currentVideo == null) {
            Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        // Setup UI
        displayVideoInfo();

        // Setup listeners
        setupListeners();

        // Play video
        viewModel.playVideo(currentVideo);

        // Observe ViewModel
        observeViewModel();

        // Log play history
        viewModel.logPlayHistory();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        thumbnail = findViewById(R.id.thumbnail);
        titleText = findViewById(R.id.title);
        artistText = findViewById(R.id.artist);
        currentTimeText = findViewById(R.id.current_time);
        totalTimeText = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seekbar);
        playButton = findViewById(R.id.play_button);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);
        likeButton = findViewById(R.id.like_button);
        playlistButton = findViewById(R.id.playlist_button);
        shareButton = findViewById(R.id.share_button);
    }

    /**
     * Display video information
     */
    private void displayVideoInfo() {
        titleText.setText(currentVideo.getTitle());
        artistText.setText(currentVideo.getArtist());
        totalTimeText.setText(formatTime(currentVideo.getDuration()));

        // Load thumbnail
        Glide.with(this)
            .load(currentVideo.getThumbnail())
            .into(thumbnail);
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        playButton.setOnClickListener(v -> handlePlayPause());
        prevButton.setOnClickListener(v -> handlePrevious());
        nextButton.setOnClickListener(v -> handleNext());
        likeButton.setOnClickListener(v -> handleLike());
        playlistButton.setOnClickListener(v -> handleAddToPlaylist());
        shareButton.setOnClickListener(v -> handleShare());

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    viewModel.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
            }
        });
    }

    /**
     * Handle play/pause button
     */
    private void handlePlayPause() {
        Boolean isPlaying = viewModel.getIsPlaying().getValue();
        if (isPlaying != null && isPlaying) {
            viewModel.pause();
            playButton.setText("▶");
        } else {
            viewModel.play();
            playButton.setText("⏸");
        }
    }

    /**
     * Handle previous button
     */
    private void handlePrevious() {
        Toast.makeText(this, "Previous video", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle next button
     */
    private void handleNext() {
        Toast.makeText(this, "Next video", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle like button
     */
    private void handleLike() {
        likeButton.setText("♥ Liked");
        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle add to playlist
     */
    private void handleAddToPlaylist() {
        Toast.makeText(this, "Add to playlist", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle share button
     */
    private void handleShare() {
        Toast.makeText(this, "Share video", Toast.LENGTH_SHORT).show();
    }

    /**
     * Observe ViewModel changes
     */
    private void observeViewModel() {
        viewModel.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                playButton.setText("⏸");
            } else {
                playButton.setText("▶");
            }
        });

        viewModel.getCurrentPosition().observe(this, position -> {
            if (!isSeeking) {
                seekBar.setProgress(position);
                currentTimeText.setText(formatTime(position));
            }
        });

        viewModel.getDuration().observe(this, duration -> {
            seekBar.setMax(duration);
            totalTimeText.setText(formatTime(duration));
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Format time in milliseconds to MM:SS
     */
    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // MediaPlayer will be released in ViewModel.onCleared()
    }
}

