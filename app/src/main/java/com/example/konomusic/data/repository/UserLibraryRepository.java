package com.example.konomusic.data.repository;

import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.domain.model.MusicFiles;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLibraryRepository {

    public interface FavoriteStateCallback {
        void onResult(boolean isFavorite);

        void onError(String message);
    }

    public interface ResultCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface PlaylistsCallback {
        void onResult(ArrayList<PlaylistItem> playlists);

        void onError(String message);
    }

    public interface FavoritesCallback {
        void onResult(ArrayList<MusicFiles> songs);

        void onError(String message);
    }

    public interface PlaylistSongsCallback {
        void onResult(ArrayList<MusicFiles> songs);

        void onError(String message);
    }

    public static class PlaylistItem {
        private final String playlistId;
        private final String name;
        private final int songCount;
        private final String coverUrl;

        public PlaylistItem(String playlistId, String name, int songCount, String coverUrl) {
            this.playlistId = playlistId;
            this.name = name;
            this.songCount = songCount;
            this.coverUrl = coverUrl;
        }

        public String getPlaylistId() {
            return playlistId;
        }

        public String getName() {
            return name;
        }

        public int getSongCount() {
            return songCount;
        }

        public String getCoverUrl() {
            return coverUrl;
        }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void isFavorite(String uid, MusicFiles song, FavoriteStateCallback callback) {
        String key = songKey(song);
        if (uid == null || uid.trim().isEmpty() || key.isEmpty()) {
            callback.onResult(false);
            return;
        }
        db.collection("users").document(uid).collection("favorites").document(key).get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot != null && snapshot.exists()))
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void toggleFavorite(String uid, MusicFiles song, FavoriteStateCallback callback) {
        String key = songKey(song);
        if (uid == null || uid.trim().isEmpty() || key.isEmpty()) {
            callback.onError("Missing user or song key");
            return;
        }

        DocumentReference ref = db.collection("users").document(uid).collection("favorites").document(key);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                ref.delete()
                        .addOnSuccessListener(v -> callback.onResult(false))
                        .addOnFailureListener(e -> callback.onError(msg(e)));
                return;
            }

            Map<String, Object> data = songPayload(song, key);
            data.put("addedAt", FieldValue.serverTimestamp());

            ref.set(data)
                    .addOnSuccessListener(v -> callback.onResult(true))
                    .addOnFailureListener(e -> callback.onError(msg(e)));
        }).addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void loadFavorites(String uid, FavoritesCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }

        db.collection("users").document(uid).collection("favorites")
                .get()
                .addOnSuccessListener(snapshots -> {
                    ArrayList<MusicFiles> out = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String songId = firstNonEmpty(doc.getString("songId"), doc.getId());
                        String title = firstNonEmpty(doc.getString("title"), "Unknown");
                        String artist = firstNonEmpty(doc.getString("artist"), "Unknown Artist");
                        String album = firstNonEmpty(doc.getString("album"), "Unknown Album");
                        String streamUrl = firstNonEmpty(doc.getString("streamUrl"), "");
                        String artworkUrl = firstNonEmpty(doc.getString("artworkUrl"), "");
                        String duration = firstNonEmpty(doc.getString("duration"), "0");

                        MusicFiles item = new MusicFiles(streamUrl, title, artist, album, duration, songId);
                        item.setSongId(songId);
                        item.setStreamUrl(streamUrl);
                        item.setArtworkUrl(artworkUrl);
                        item.setFromFirebase(true);
                        out.add(item);
                    }
                    callback.onResult(out);
                })
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void loadPlaylists(String uid, PlaylistsCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        db.collection("users").document(uid).collection("playlists")
                .get()
                .addOnSuccessListener(snapshots -> {
                    ArrayList<PlaylistItem> out = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        if (name == null || name.trim().isEmpty()) {
                            name = id;
                        }

                        List<?> songIds = (List<?>) doc.get("songIds");
                        int count = songIds == null ? 0 : songIds.size();
                        String coverUrl = firstNonEmpty(doc.getString("coverUrl"), "");

                        out.add(new PlaylistItem(id, name.trim(), count, coverUrl));
                    }
                    callback.onResult(out);
                })
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void createPlaylistAndAddSong(String uid, String playlistName, MusicFiles song, ResultCallback callback) {
        String key = songKey(song);
        if (uid == null || uid.trim().isEmpty() || key.isEmpty()) {
            callback.onError("Missing user or song key");
            return;
        }

        String cleanName = playlistName == null ? "" : playlistName.trim();
        if (cleanName.isEmpty()) {
            callback.onError("Playlist name is empty");
            return;
        }

        String playlistId = "pl_" + System.currentTimeMillis();
        DocumentReference ref = db.collection("users").document(uid).collection("playlists").document(playlistId);

        Map<String, Object> data = new HashMap<>();
        data.put("playlistId", playlistId);
        data.put("name", cleanName);
        data.put("description", "");
        data.put("coverUrl", firstNonEmpty(song.getArtworkUrl(), ""));
        data.put("isPublic", false);
        data.put("songIds", FieldValue.arrayUnion(key));
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.set(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void addSongToPlaylist(String uid, String playlistId, MusicFiles song, ResultCallback callback) {
        String key = songKey(song);
        if (uid == null || uid.trim().isEmpty() || playlistId == null || playlistId.trim().isEmpty() || key.isEmpty()) {
            callback.onError("Missing user, playlist or song key");
            return;
        }

        DocumentReference ref = db.collection("users").document(uid).collection("playlists").document(playlistId.trim());
        Map<String, Object> data = new HashMap<>();
        data.put("songIds", FieldValue.arrayUnion(key));
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.set(data, SetOptions.merge())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void removeSongFromPlaylist(String uid, String playlistId, MusicFiles song, ResultCallback callback) {
        String key = songKey(song);
        if (uid == null || uid.trim().isEmpty() || playlistId == null || playlistId.trim().isEmpty() || key.isEmpty()) {
            callback.onError("Missing user, playlist or song key");
            return;
        }

        DocumentReference ref = db.collection("users").document(uid).collection("playlists").document(playlistId.trim());
        Map<String, Object> data = new HashMap<>();
        data.put("songIds", FieldValue.arrayRemove(key));
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.set(data, SetOptions.merge())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void renamePlaylist(String uid, String playlistId, String newName, ResultCallback callback) {
        if (uid == null || uid.trim().isEmpty() || playlistId == null || playlistId.trim().isEmpty()) {
            callback.onError("Missing user or playlist");
            return;
        }
        String name = newName == null ? "" : newName.trim();
        if (name.isEmpty()) {
            callback.onError("Playlist name is empty");
            return;
        }

        DocumentReference ref = db.collection("users").document(uid).collection("playlists").document(playlistId.trim());
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.set(data, SetOptions.merge())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void deletePlaylist(String uid, String playlistId, ResultCallback callback) {
        if (uid == null || uid.trim().isEmpty() || playlistId == null || playlistId.trim().isEmpty()) {
            callback.onError("Missing user or playlist");
            return;
        }

        db.collection("users").document(uid).collection("playlists").document(playlistId.trim())
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public void loadPlaylistSongs(String uid, String playlistId, PlaylistSongsCallback callback) {
        if (uid == null || uid.trim().isEmpty() || playlistId == null || playlistId.trim().isEmpty()) {
            callback.onResult(new ArrayList<>());
            return;
        }

        db.collection("users").document(uid).collection("playlists").document(playlistId.trim()).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    java.util.List<?> rawIds = (java.util.List<?>) snapshot.get("songIds");
                    if (rawIds == null || rawIds.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    ArrayList<String> ids = new ArrayList<>();
                    for (Object id : rawIds) {
                        if (id != null) {
                            String key = String.valueOf(id).trim();
                            if (!key.isEmpty()) {
                                ids.add(key);
                            }
                        }
                    }

                    if (ids.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    ArrayList<MusicFiles> fromMain = new ArrayList<>();
                    ArrayList<String> missing = new ArrayList<>();

                    for (String id : ids) {
                        MusicFiles cached = findInMainList(id);
                        if (cached != null) {
                            fromMain.add(cached);
                        } else {
                            missing.add(id);
                        }
                    }

                    if (missing.isEmpty()) {
                        callback.onResult(fromMain);
                        return;
                    }

                    db.collection("songs").get().addOnSuccessListener(songSnapshots -> {
                        Map<String, MusicFiles> byKey = new HashMap<>();
                        for (QueryDocumentSnapshot doc : songSnapshots) {
                            MusicFiles item = parseSongDoc(doc);
                            if (item == null) {
                                continue;
                            }
                            String key = songKey(item);
                            if (!key.isEmpty()) {
                                byKey.put(key, item);
                            }
                            byKey.put(doc.getId(), item);
                        }

                        ArrayList<MusicFiles> out = new ArrayList<>();
                        for (String id : ids) {
                            MusicFiles cached = findInMainList(id);
                            if (cached != null) {
                                out.add(cached);
                                continue;
                            }
                            MusicFiles remote = byKey.get(id);
                            if (remote != null) {
                                out.add(remote);
                            }
                        }
                        callback.onResult(out);
                    }).addOnFailureListener(e -> callback.onError(msg(e)));
                })
                .addOnFailureListener(e -> callback.onError(msg(e)));
    }

    public static String songKey(MusicFiles song) {
        if (song == null) {
            return "";
        }
        if (song.getSongId() != null && !song.getSongId().trim().isEmpty()) {
            return song.getSongId().trim();
        }
        if (song.getId() != null && !song.getId().trim().isEmpty()) {
            return song.getId().trim();
        }
        if (song.getStreamUrl() != null && !song.getStreamUrl().trim().isEmpty()) {
            return song.getStreamUrl().trim();
        }
        if (song.getPath() != null && !song.getPath().trim().isEmpty()) {
            return song.getPath().trim();
        }
        return "";
    }

    private Map<String, Object> songPayload(MusicFiles song, String key) {
        Map<String, Object> data = new HashMap<>();
        data.put("songId", key);
        data.put("title", firstNonEmpty(song != null ? song.getTitle() : null, "Unknown"));
        data.put("artist", firstNonEmpty(song != null ? song.getArtist() : null, "Unknown Artist"));
        data.put("album", firstNonEmpty(song != null ? song.getAlbum() : null, "Unknown Album"));
        data.put("artworkUrl", firstNonEmpty(song != null ? song.getArtworkUrl() : null, ""));
        data.put("duration", firstNonEmpty(song != null ? song.getDuration() : null, "0"));
        data.put("streamUrl", firstNonEmpty(song != null ? song.getStreamUrl() : null, song != null ? song.getPath() : ""));
        return data;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String msg(Exception e) {
        return e != null && e.getMessage() != null ? e.getMessage() : "Unknown error";
    }

    private MusicFiles parseSongDoc(QueryDocumentSnapshot doc) {
        if (doc == null) {
            return null;
        }
        String songId = firstNonEmpty(doc.getString("songId"), doc.getId());
        String title = firstNonEmpty(doc.getString("title"), "Unknown");
        String artist = firstNonEmpty(doc.getString("artist"), "Unknown Artist");
        String album = firstNonEmpty(doc.getString("album"), "Unknown Album");
        String duration = firstNonEmpty(doc.getString("duration"), "0");
        String streamUrl = firstNonEmpty(doc.getString("streamUrl"), "");
        String artworkUrl = firstNonEmpty(doc.getString("artworkUrl"), "");

        MusicFiles song = new MusicFiles(streamUrl, title, artist, album, duration, songId);
        song.setSongId(songId);
        song.setStreamUrl(streamUrl);
        song.setArtworkUrl(artworkUrl);
        song.setFromFirebase(true);
        return song;
    }

    private MusicFiles findInMainList(String key) {
        if (key == null || key.trim().isEmpty() || MainActivity.musicFiles == null) {
            return null;
        }
        String clean = key.trim();
        for (MusicFiles item : MainActivity.musicFiles) {
            if (item == null) {
                continue;
            }
            String itemKey = songKey(item);
            if (!itemKey.isEmpty() && itemKey.equals(clean)) {
                return item;
            }
            if (item.getId() != null && item.getId().trim().equals(clean)) {
                return item;
            }
        }
        return null;
    }
}
