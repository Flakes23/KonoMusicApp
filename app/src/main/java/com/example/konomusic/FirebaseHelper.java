package com.example.konomusic;

import android.text.TextUtils;
import android.util.Log;

import com.example.konomusic.domain.model.CategoryItem;
import com.example.konomusic.domain.model.MusicFiles;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;
    private final Map<String, String> artistNameById = new HashMap<>();
    private final Map<String, String> albumNameById = new HashMap<>();
    private final ArrayList<Runnable> pendingArtistCacheCallbacks = new ArrayList<>();
    private boolean artistCacheLoaded = false;
    private boolean artistCacheLoading = false;
    private boolean albumCacheLoaded = false;
    private boolean albumCacheLoading = false;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void loadSongsFromFirebase(final OnSongsLoadedListener listener) {
        ensureArtistCacheLoaded(() -> db.collection("songs").addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Error loading songs", error);
                listener.onError(error.getMessage());
                return;
            }
            listener.onSongsLoaded(parseSongs(snapshots));
        }));
    }

    public void loadCategoryItems(String type, final OnCategoryItemsLoadedListener listener) {
        String collection;
        switch (safeLower(type)) {
            case "album":
                collection = "albums";
                break;
            case "artist":
                collection = "artists";
                break;
            case "genre":
                collection = "genres";
                break;
            default:
                listener.onError("Unsupported category type: " + type);
                return;
        }

        db.collection(collection)
                .get()
                .addOnSuccessListener(snapshots -> {
                    ArrayList<CategoryItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String name = firstNonEmpty(getAsString(doc, "name"), getAsString(doc, type));
                        if (TextUtils.isEmpty(name)) {
                            continue;
                        }
                        String idFromField = firstNonEmpty(
                                getAsString(doc, type + "Id"),
                                getAsString(doc, "id"),
                                getAsString(doc, "docId")
                        );
                        String id = firstNonEmpty(idFromField, doc.getId(), name);
                        String image = firstNonEmpty(
                                getAsString(doc, "imageUrl"),
                                getAsString(doc, type + "ImageUrl"),
                                getAsString(doc, "artworkUrl")
                        );
                        items.add(new CategoryItem(id, name, image, safeLower(type)));
                    }
                    listener.onLoaded(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories: " + collection, e);
                    listener.onError(e.getMessage());
                });
    }

    public void loadSongsByCategory(String type, String categoryId, String categoryName, final OnSongsLoadedListener listener) {
        if (!artistCacheLoaded || !albumCacheLoaded) {
            ensureArtistCacheLoaded(() -> loadSongsByCategory(type, categoryId, categoryName, listener));
            return;
        }

        String normalized = safeLower(type);
        if (TextUtils.isEmpty(normalized)) {
            listener.onSongsLoaded(new ArrayList<>());
            return;
        }

        if ("album".equals(normalized) && !TextUtils.isEmpty(categoryId)) {
            querySongsByAlbumId(categoryId, categoryName, listener);
            return;
        }

        String idField = normalized + "Id";
        String nameField = normalized;
        Query songs = db.collection("songs");

        if (!TextUtils.isEmpty(categoryId)) {
            songs.whereEqualTo(idField, categoryId)
                    .get()
                    .addOnSuccessListener(idResult -> {
                        if (idResult != null && !idResult.isEmpty()) {
                            listener.onSongsLoaded(parseSongs(idResult));
                            return;
                        }

                        // Handle schemas where ids are stored as numbers in Firestore.
                        Long numericId = tryParseLong(categoryId);
                        if (numericId != null) {
                            songs.whereEqualTo(idField, numericId)
                                    .get()
                                    .addOnSuccessListener(numResult -> {
                                        if (numResult != null && !numResult.isEmpty()) {
                                            listener.onSongsLoaded(parseSongs(numResult));
                                        } else {
                                            queryByNameFallback(songs, nameField, categoryName, listener);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error query songs by numeric id", e);
                                        queryByNameFallback(songs, nameField, categoryName, listener);
                                    });
                        } else {
                            queryByNameFallback(songs, nameField, categoryName, listener);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error query songs by id", e);
                        queryByNameFallback(songs, nameField, categoryName, listener);
                    });
            return;
        }

        queryByNameFallback(songs, nameField, categoryName, listener);
    }

    private void querySongsByAlbumId(String categoryId, String categoryName, final OnSongsLoadedListener listener) {
        Query songs = db.collection("songs");

        songs.whereArrayContains("albumId", categoryId)
                .get()
                .addOnSuccessListener(arrayResult -> {
                    if (arrayResult != null && !arrayResult.isEmpty()) {
                        listener.onSongsLoaded(parseSongs(arrayResult));
                        return;
                    }

                    Long numericId = tryParseLong(categoryId);
                    if (numericId != null) {
                        songs.whereArrayContains("albumId", numericId)
                                .get()
                                .addOnSuccessListener(numberArrayResult -> {
                                    if (numberArrayResult != null && !numberArrayResult.isEmpty()) {
                                        listener.onSongsLoaded(parseSongs(numberArrayResult));
                                    } else {
                                        querySongsByAlbumIdLegacyScalar(songs, categoryId, numericId, categoryName, listener);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error query songs by albumId numeric array", e);
                                    querySongsByAlbumIdLegacyScalar(songs, categoryId, numericId, categoryName, listener);
                                });
                    } else {
                        querySongsByAlbumIdLegacyScalar(songs, categoryId, null, categoryName, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error query songs by albumId array", e);
                    querySongsByAlbumIdLegacyScalar(songs, categoryId, tryParseLong(categoryId), categoryName, listener);
                });
    }

    private void querySongsByAlbumIdLegacyScalar(Query songs,
                                                  String categoryId,
                                                  Long numericId,
                                                  String categoryName,
                                                  final OnSongsLoadedListener listener) {
        songs.whereEqualTo("albumId", categoryId)
                .get()
                .addOnSuccessListener(stringResult -> {
                    if (stringResult != null && !stringResult.isEmpty()) {
                        listener.onSongsLoaded(parseSongs(stringResult));
                        return;
                    }
                    if (numericId != null) {
                        songs.whereEqualTo("albumId", numericId)
                                .get()
                                .addOnSuccessListener(numberResult -> {
                                    if (numberResult != null && !numberResult.isEmpty()) {
                                        listener.onSongsLoaded(parseSongs(numberResult));
                                    } else {
                                        queryByNameFallback(songs, "album", categoryName, listener);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error query songs by albumId numeric scalar", e);
                                    queryByNameFallback(songs, "album", categoryName, listener);
                                });
                    } else {
                        queryByNameFallback(songs, "album", categoryName, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error query songs by albumId scalar", e);
                    queryByNameFallback(songs, "album", categoryName, listener);
                });
    }

    private void queryByNameFallback(Query base, String nameField, String categoryName, final OnSongsLoadedListener listener) {
        if (TextUtils.isEmpty(categoryName)) {
            listener.onSongsLoaded(new ArrayList<>());
            return;
        }

        base.whereEqualTo(nameField, categoryName)
                .get()
                .addOnSuccessListener(nameResult -> listener.onSongsLoaded(parseSongs(nameResult)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error query songs by name", e);
                    listener.onError(e.getMessage());
                });
    }

    private void ensureArtistCacheLoaded(Runnable onReady) {
        if (onReady != null) {
            pendingArtistCacheCallbacks.add(onReady);
        }

        if (artistCacheLoaded && albumCacheLoaded) {
            flushPendingArtistCacheCallbacks();
            return;
        }
        startArtistCacheLoadIfNeeded();
        startAlbumCacheLoadIfNeeded();
    }

    private void startArtistCacheLoadIfNeeded() {
        if (artistCacheLoaded || artistCacheLoading) {
            return;
        }

        artistCacheLoading = true;
        db.collection("artists")
                .get()
                .addOnSuccessListener(this::rebuildArtistCache)
                .addOnFailureListener(e -> Log.w(TAG, "Error loading artist cache", e))
                .addOnCompleteListener(task -> {
                    artistCacheLoading = false;
                    artistCacheLoaded = true;
                    onReferenceCachesMaybeReady();
                });
    }

    private void startAlbumCacheLoadIfNeeded() {
        if (albumCacheLoaded || albumCacheLoading) {
            return;
        }

        albumCacheLoading = true;
        db.collection("albums")
                .get()
                .addOnSuccessListener(this::rebuildAlbumCache)
                .addOnFailureListener(e -> Log.w(TAG, "Error loading album cache", e))
                .addOnCompleteListener(task -> {
                    albumCacheLoading = false;
                    albumCacheLoaded = true;
                    onReferenceCachesMaybeReady();
                });
    }

    private void onReferenceCachesMaybeReady() {
        if (artistCacheLoaded && albumCacheLoaded) {
            flushPendingArtistCacheCallbacks();
            return;
        }
        if (!artistCacheLoaded) {
            startArtistCacheLoadIfNeeded();
        }
        if (!albumCacheLoaded) {
            startAlbumCacheLoadIfNeeded();
        }
    }

    private void rebuildArtistCache(QuerySnapshot snapshots) {
        artistNameById.clear();
        if (snapshots == null) {
            return;
        }

        for (QueryDocumentSnapshot doc : snapshots) {
            String artistName = firstNonEmpty(getAsString(doc, "name"), getAsString(doc, "artist"));
            if (TextUtils.isEmpty(artistName)) {
                continue;
            }

            String artistId = firstNonEmpty(
                    getAsString(doc, "artistId"),
                    getAsString(doc, "id"),
                    getAsString(doc, "docId"),
                    doc.getId()
            );
            String key = normalizeIdKey(artistId);
            if (!TextUtils.isEmpty(key)) {
                artistNameById.put(key, artistName);
            }
        }
    }

    private void rebuildAlbumCache(QuerySnapshot snapshots) {
        albumNameById.clear();
        if (snapshots == null) {
            return;
        }

        for (QueryDocumentSnapshot doc : snapshots) {
            String albumName = firstNonEmpty(getAsString(doc, "name"), getAsString(doc, "album"));
            if (TextUtils.isEmpty(albumName)) {
                continue;
            }

            String albumId = firstNonEmpty(
                    getAsString(doc, "albumId"),
                    getAsString(doc, "id"),
                    getAsString(doc, "docId"),
                    doc.getId()
            );
            String key = normalizeIdKey(albumId);
            if (!TextUtils.isEmpty(key)) {
                albumNameById.put(key, albumName);
            }
        }
    }

    private void flushPendingArtistCacheCallbacks() {
        if (pendingArtistCacheCallbacks.isEmpty()) {
            return;
        }
        ArrayList<Runnable> callbacks = new ArrayList<>(pendingArtistCacheCallbacks);
        pendingArtistCacheCallbacks.clear();
        for (Runnable callback : callbacks) {
            try {
                callback.run();
            } catch (Exception ignored) {
            }
        }
    }

    private String resolveArtistName(String artistFromSong, String artistId) {
        String explicitArtist = firstNonEmpty(artistFromSong);
        if (!TextUtils.isEmpty(explicitArtist) && !"unknown artist".equalsIgnoreCase(explicitArtist)) {
            return explicitArtist;
        }

        String key = normalizeIdKey(artistId);
        if (!TextUtils.isEmpty(key)) {
            String fromCache = artistNameById.get(key);
            if (!TextUtils.isEmpty(fromCache)) {
                return fromCache;
            }
        }

        return firstNonEmpty(explicitArtist, "Unknown Artist");
    }

    private String resolveAlbumName(String albumFromSong, ArrayList<String> albumIds) {
        String explicitAlbum = firstNonEmpty(albumFromSong);
        if (!TextUtils.isEmpty(explicitAlbum)
                && !"unknown album".equalsIgnoreCase(explicitAlbum)
                && !"unknown".equalsIgnoreCase(explicitAlbum)) {
            return explicitAlbum;
        }

        if (albumIds != null) {
            for (String rawAlbumId : albumIds) {
                String key = normalizeIdKey(rawAlbumId);
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                String fromCache = albumNameById.get(key);
                if (!TextUtils.isEmpty(fromCache)) {
                    return fromCache;
                }
            }
        }

        return firstNonEmpty(explicitAlbum, "Unknown Album");
    }

    private String normalizeIdKey(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value.trim();
    }

    private ArrayList<MusicFiles> parseSongs(QuerySnapshot snapshots) {
        ArrayList<MusicFiles> firebaseSongs = new ArrayList<>();
        if (snapshots == null) {
            return firebaseSongs;
        }

        for (QueryDocumentSnapshot doc : snapshots) {
            try {
                String docId = doc.getId();
                String songId = firstNonEmpty(getAsString(doc, "songId"), docId);
                String title = firstNonEmpty(getAsString(doc, "title"), "Unknown");
                ArrayList<String> albumId = getAsStringList(doc.get("albumId"));
                String artistId = firstNonEmpty(getAsString(doc, "artistId"));
                String artist = resolveArtistName(getAsString(doc, "artist"), artistId);
                String album = resolveAlbumName(getAsString(doc, "album"), albumId);
                String duration = firstNonEmpty(getAsString(doc, "duration"), "0");
                String streamUrl = firstNonEmpty(getAsString(doc, "streamUrl"), "");
                String artworkUrl = firstNonEmpty(getAsString(doc, "artworkUrl"), "");
                String genre = firstNonEmpty(getAsString(doc, "genre"), getAsString(doc, "gender"), "Unknown");
                Long playCount = doc.getLong("playCount");

                // id = Firestore document id for direct updates to /songs/{id}
                MusicFiles musicFile = new MusicFiles(streamUrl, title, artist, album, duration, docId);
                musicFile.setId(docId);
                musicFile.setSongId(songId);
                musicFile.setStreamUrl(streamUrl);
                musicFile.setArtworkUrl(artworkUrl);
                musicFile.setFromFirebase(true);
                musicFile.setGenre(genre);
                musicFile.setPlayCount(playCount != null ? playCount : 0L);

                String genreId = firstNonEmpty(getAsString(doc, "genreId"));
                musicFile.setAlbumId(albumId);
                musicFile.setArtistId(artistId);
                musicFile.setGenreId(genreId);

                musicFile.setAlbumImageUrl(firstNonEmpty(getAsString(doc, "albumImageUrl"), artworkUrl));
                musicFile.setArtistImageUrl(firstNonEmpty(getAsString(doc, "artistImageUrl"), artworkUrl));
                musicFile.setGenreImageUrl(firstNonEmpty(getAsString(doc, "genreImageUrl"), artworkUrl));

                firebaseSongs.add(musicFile);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing song", e);
            }
        }
        return firebaseSongs;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String getAsString(QueryDocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            double asDouble = number.doubleValue();
            long asLong = number.longValue();
            if (asDouble == (double) asLong) {
                return String.valueOf(asLong);
            }
            return String.valueOf(asDouble);
        }
        return String.valueOf(value).trim();
    }

    private Long tryParseLong(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private ArrayList<String> getAsStringList(Object value) {
        ArrayList<String> out = new ArrayList<>();
        if (value == null) {
            return out;
        }

        if (value instanceof List<?>) {
            for (Object raw : (List<?>) value) {
                String normalized = normalizeScalarValue(raw);
                if (!TextUtils.isEmpty(normalized) && !out.contains(normalized)) {
                    out.add(normalized);
                }
            }
            return out;
        }

        String normalized = normalizeScalarValue(value);
        if (!TextUtils.isEmpty(normalized)) {
            out.add(normalized);
        }
        return out;
    }

    private String normalizeScalarValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            double asDouble = number.doubleValue();
            long asLong = number.longValue();
            if (asDouble == (double) asLong) {
                return String.valueOf(asLong);
            }
            return String.valueOf(asDouble);
        }
        return String.valueOf(value).trim();
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(ArrayList<MusicFiles> songs);

        void onError(String errorMessage);
    }

    public interface OnCategoryItemsLoadedListener {
        void onLoaded(ArrayList<CategoryItem> items);

        void onError(String errorMessage);
    }
}
