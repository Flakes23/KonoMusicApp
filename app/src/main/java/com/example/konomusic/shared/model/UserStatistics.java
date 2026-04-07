package com.example.konomusic.shared.model;

/**
 * User Statistics Model
 */
public class UserStatistics {
    private int totalPlaylists;
    private long totalPlayTime;
    private int totalPlays;
    private int favoriteCount;

    public UserStatistics() {
    }

    public UserStatistics(int totalPlaylists, long totalPlayTime, int totalPlays, int favoriteCount) {
        this.totalPlaylists = totalPlaylists;
        this.totalPlayTime = totalPlayTime;
        this.totalPlays = totalPlays;
        this.favoriteCount = favoriteCount;
    }

    // Getters and Setters
    public int getTotalPlaylists() { return totalPlaylists; }
    public void setTotalPlaylists(int totalPlaylists) { this.totalPlaylists = totalPlaylists; }

    public long getTotalPlayTime() { return totalPlayTime; }
    public void setTotalPlayTime(long totalPlayTime) { this.totalPlayTime = totalPlayTime; }

    public int getTotalPlays() { return totalPlays; }
    public void setTotalPlays(int totalPlays) { this.totalPlays = totalPlays; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
}

