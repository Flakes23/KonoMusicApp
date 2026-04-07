package com.example.konomusic.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 * Playlist Model - Danh sách phát
 */
public class Playlist implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private List<Video> videos;
    private String createdAt;

    // Constructor
    public Playlist() {}

    public Playlist(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Video> getVideos() { return videos; }
    public void setVideos(List<Video> videos) { this.videos = videos; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Aliases for compatibility
    public int getVideoCount() { return (int)(videos != null ? videos.size() : 0); }
}
