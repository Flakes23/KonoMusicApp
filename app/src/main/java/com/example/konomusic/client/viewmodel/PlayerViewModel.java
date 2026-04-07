package com.example.konomusic.client.viewmodel;

import android.app.Application;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.konomusic.shared.model.Video;

/**
 * PlayerViewModel - Handle audio playback
 * By: HUY
 */
public class PlayerViewModel extends AndroidViewModel {
    private final MutableLiveData<Video> currentVideo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<Float> playbackSpeed = new MutableLiveData<>(1.0f);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private MediaPlayer mediaPlayer;
    private UpdatePositionRunnable updateRunnable;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Initialize and play video
     */
    public void playVideo(Video video) {
        currentVideo.setValue(video);

        // Setup MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(video.getPreviewUrl());
            mediaPlayer.setOnPreparedListener(mp -> {
                duration.setValue(mp.getDuration());
                mp.start();
                isPlaying.setValue(true);
                startUpdatingPosition();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying.setValue(false);
                stopUpdatingPosition();
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            errorMessage.setValue("Error: " + e.getMessage());
        }
    }

    /**
     * Play
     */
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying.setValue(true);
            startUpdatingPosition();
        }
    }

    /**
     * Pause
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying.setValue(false);
            stopUpdatingPosition();
        }
    }

    /**
     * Resume
     */
    public void resume() {
        play();
    }

    /**
     * Seek to position
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            currentPosition.setValue(position);
        }
    }

    /**
     * Log play history
     */
    public void logPlayHistory() {
        // TODO: Call API to log play history
        Video video = currentVideo.getValue();
        if (video != null) {
            // POST /api/play-logs with videoId and playedAt
        }
    }

    /**
     * Start updating position
     */
    private void startUpdatingPosition() {
        if (updateRunnable == null) {
            updateRunnable = new UpdatePositionRunnable();
            new Thread(updateRunnable).start();
        }
    }

    /**
     * Stop updating position
     */
    private void stopUpdatingPosition() {
        if (updateRunnable != null) {
            updateRunnable.stop();
            updateRunnable = null;
        }
    }

    /**
     * Runnable to update position
     */
    private class UpdatePositionRunnable implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            while (running && mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    currentPosition.postValue(mediaPlayer.getCurrentPosition());
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopUpdatingPosition();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    // Getters
    public LiveData<Video> getCurrentVideo() { return currentVideo; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Integer> getCurrentPosition() { return currentPosition; }
    public LiveData<Integer> getDuration() { return duration; }
    public LiveData<Float> getPlaybackSpeed() { return playbackSpeed; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

