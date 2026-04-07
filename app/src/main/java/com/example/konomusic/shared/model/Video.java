package com.example.konomusic.shared.model;

import java.io.Serializable;

/**
 * Video Model - Đại diện cho bài hát/video
 */
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String youtubeId;
    private String title;
    private String channelName;
    private Integer durationMs;
    private Long viewCount;
    private String thumbnailUrl;
    private String fetchedAt;

    // Constructor
    public Video() {}

    public Video(Long id, String youtubeId, String title, String channelName,
                 Integer durationMs, Long viewCount, String thumbnailUrl) {
        this.id = id;
        this.youtubeId = youtubeId;
        this.title = title;
        this.channelName = channelName;
        this.durationMs = durationMs;
        this.viewCount = viewCount;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(String fetchedAt) { this.fetchedAt = fetchedAt; }

    // Aliases for compatibility
    public String getArtist() { return channelName; }
    public String getThumbnail() { return thumbnailUrl; }
    public int getDuration() { return (int)(durationMs / 1000); }
    public String getPreviewUrl() { return youtubeId; }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", youtubeId='" + youtubeId + '\'' +
                ", title='" + title + '\'' +
                ", channelName='" + channelName + '\'' +
                ", durationMs=" + durationMs +
                ", viewCount=" + viewCount +
                '}';
    }

}
