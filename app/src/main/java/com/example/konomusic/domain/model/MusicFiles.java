package com.example.konomusic.domain.model;

import java.util.ArrayList;
import java.util.List;

public class MusicFiles {
    private String path;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String id;
    private String streamUrl;      // URL từ Firebase để stream
    private String artworkUrl;     // URL ảnh cover từ Firebase
    private boolean isFromFirebase; // Flag để biết bài từ Firebase hay local
    private String genre;
    private long playCount;
    private String songId;
    private ArrayList<String> albumId;
    private String artistId;
    private String genreId;
    private String albumImageUrl;
    private String artistImageUrl;
    private String genreImageUrl;


    public MusicFiles(String path, String title, String artist, String album, String duration, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.isFromFirebase = false;
        this.genre = "Unknown";
        this.playCount = 0L;
        this.songId = "";
        this.albumId = new ArrayList<>();
        this.artistId = "";
        this.genreId = "";
        this.albumImageUrl = "";
        this.artistImageUrl = "";
        this.genreImageUrl = "";
    }

    public MusicFiles() {
        this.genre = "Unknown";
        this.playCount = 0L;
        this.songId = "";
        this.albumId = new ArrayList<>();
        this.artistId = "";
        this.genreId = "";
        this.albumImageUrl = "";
        this.artistImageUrl = "";
        this.genreImageUrl = "";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }


    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public boolean isFromFirebase() {
        return isFromFirebase;
    }

    public void setFromFirebase(boolean fromFirebase) {
        isFromFirebase = fromFirebase;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public ArrayList<String> getAlbumId() {
        return new ArrayList<>(albumId);
    }

    public void setAlbumId(String albumId) {
        this.albumId.clear();
        if (albumId != null) {
            String value = albumId.trim();
            if (!value.isEmpty()) {
                this.albumId.add(value);
            }
        }
    }

    public void setAlbumId(List<String> albumId) {
        this.albumId.clear();
        if (albumId == null) {
            return;
        }
        for (String value : albumId) {
            if (value == null) {
                continue;
            }
            String normalized = value.trim();
            if (!normalized.isEmpty() && !this.albumId.contains(normalized)) {
                this.albumId.add(normalized);
            }
        }
    }

    public String getPrimaryAlbumId() {
        return albumId.isEmpty() ? "" : albumId.get(0);
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getGenreId() {
        return genreId;
    }

    public void setGenreId(String genreId) {
        this.genreId = genreId;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public void setAlbumImageUrl(String albumImageUrl) {
        this.albumImageUrl = albumImageUrl;
    }

    public String getArtistImageUrl() {
        return artistImageUrl;
    }

    public void setArtistImageUrl(String artistImageUrl) {
        this.artistImageUrl = artistImageUrl;
    }

    public String getGenreImageUrl() {
        return genreImageUrl;
    }

    public void setGenreImageUrl(String genreImageUrl) {
        this.genreImageUrl = genreImageUrl;
    }
}
