package com.example.konomusic.shared.model;

/**
 * PlayLog Model - Lịch sử phát nhạc
 */
public class PlayLog {

    private Long id;
    private Long videoId;
    private Long userId;
    private String playedAt;

    // Constructor
    public PlayLog() {}

    public PlayLog(Long videoId, Long userId) {
        this.videoId = videoId;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPlayedAt() { return playedAt; }
    public void setPlayedAt(String playedAt) { this.playedAt = playedAt; }

}

