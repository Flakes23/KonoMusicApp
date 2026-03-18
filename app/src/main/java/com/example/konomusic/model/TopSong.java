package com.example.konomusic.model;

public class TopSong {
    private int rank;
    private String title;
    private String artist;
    private String playCount;

    public TopSong(int rank, String title, String artist, String playCount) {
        this.rank = rank;
        this.title = title;
        this.artist = artist;
        this.playCount = playCount;
    }

    public int getRank() { return rank; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPlayCount() { return playCount; }
}

