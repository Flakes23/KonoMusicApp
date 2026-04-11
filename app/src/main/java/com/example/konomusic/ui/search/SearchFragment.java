package com.example.konomusic.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.domain.model.CategoryItem;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.album.CategoryDetailActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private static final int MAX_SONG_RESULTS = 3;
    private static final int MAX_CATEGORY_RESULTS = 3;
    private static final long SEARCH_DEBOUNCE_MS = 80L;
    private static SearchFragment instance;

    private EditText searchInput;
    private TextView emptyText;
    private TextView songsTitle;
    private TextView artistsTitle;
    private TextView albumsTitle;
    private RecyclerView songsRecycler;
    private RecyclerView artistsRecycler;
    private RecyclerView albumsRecycler;
    private androidx.core.widget.NestedScrollView searchScroll;
    private View loadingView;

    private SearchSongAdapter songAdapter;
    private SearchCategoryAdapter artistAdapter;
    private SearchCategoryAdapter albumAdapter;

    private final ArrayList<MusicFiles> allSongs = new ArrayList<>();
    private final ArrayList<SongIndex> indexedSongs = new ArrayList<>();
    private final ArrayList<MusicFiles> filteredSongs = new ArrayList<>();
    private final ArrayList<CategoryItem> filteredArtists = new ArrayList<>();
    private final ArrayList<CategoryItem> filteredAlbums = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private volatile boolean indexReady = false;
    private int rebuildToken = 0;

    private android.view.animation.AlphaAnimation skeletonAnim;

    public SearchFragment() {
    }

    public static void refreshSearchContent() {
        if (instance != null) {
            instance.onMusicLibraryChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        instance = this;

        searchInput = root.findViewById(R.id.search_input);
        emptyText = root.findViewById(R.id.search_empty);
        songsTitle = root.findViewById(R.id.search_songs_title);
        artistsTitle = root.findViewById(R.id.search_artists_title);
        albumsTitle = root.findViewById(R.id.search_albums_title);
        songsRecycler = root.findViewById(R.id.search_songs_recycler);
        artistsRecycler = root.findViewById(R.id.search_artists_recycler);
        albumsRecycler = root.findViewById(R.id.search_albums_recycler);
        searchScroll = root.findViewById(R.id.search_scroll);
        loadingView = root.findViewById(R.id.search_loading);

        songsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        artistsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        albumsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        songsRecycler.setHasFixedSize(true);
        artistsRecycler.setHasFixedSize(true);
        albumsRecycler.setHasFixedSize(true);

        songAdapter = new SearchSongAdapter(getContext(), filteredSongs);
        artistAdapter = new SearchCategoryAdapter(filteredArtists, item -> openCategoryPage("artist", item));
        albumAdapter = new SearchCategoryAdapter(filteredAlbums, item -> openCategoryPage("album", item));

        songsRecycler.setAdapter(songAdapter);
        artistsRecycler.setAdapter(artistAdapter);
        albumsRecycler.setAdapter(albumAdapter);

        rebuildIndexAsync(() -> applyFilter(""));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        String q = searchInput == null ? "" : searchInput.getText().toString();
        if (!indexReady || indexedSongs.isEmpty()) {
            rebuildIndexAsync(() -> applyFilter(q));
        } else {
            applyFilter(q);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        if (instance == this) {
            instance = null;
        }
        rebuildToken++;
        indexReady = false;
    }

    private void scheduleFilter(String query) {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        pendingSearch = () -> {
            if (indexReady) {
                applyFilter(query);
            }
        };
        searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private void onMusicLibraryChanged() {
        if (!isAdded()) {
            return;
        }
        String q = searchInput == null ? "" : searchInput.getText().toString();
        rebuildIndexAsync(() -> applyFilter(q));
    }

    private void rebuildIndexAsync(Runnable afterBuild) {
        if (!isAdded()) {
            return;
        }
        final int token = ++rebuildToken;
        indexReady = false;
        setLoading(true);

        new Thread(() -> {
            ArrayList<MusicFiles> source = new ArrayList<>();
            ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
            if (mainSongs != null) {
                source.addAll(mainSongs);
            }

            ArrayList<SongIndex> built = new ArrayList<>();
            for (MusicFiles song : source) {
                if (song == null) {
                    continue;
                }
                String haystack = normalizeForSearch(
                        safe(song.getTitle()) + " "
                                + safe(song.getArtist()) + " "
                                + safe(song.getAlbum()) + " "
                                + safe(song.getGenre())
                );
                built.add(new SongIndex(song, haystack));
            }

            searchHandler.post(() -> {
                if (!isAdded() || token != rebuildToken) {
                    return;
                }
                allSongs.clear();
                allSongs.addAll(source);
                indexedSongs.clear();
                indexedSongs.addAll(built);
                indexReady = true;
                setLoading(false);
                if (afterBuild != null) {
                    afterBuild.run();
                }
            });
        }).start();
    }

    private void setLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                if (skeletonAnim == null) {
                    skeletonAnim = new android.view.animation.AlphaAnimation(0.45f, 1f);
                    skeletonAnim.setDuration(650);
                    skeletonAnim.setRepeatMode(android.view.animation.Animation.REVERSE);
                    skeletonAnim.setRepeatCount(android.view.animation.Animation.INFINITE);
                }
                loadingView.startAnimation(skeletonAnim);
            } else {
                loadingView.clearAnimation();
            }
        }
        if (searchScroll != null) {
            searchScroll.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
        if (show && emptyText != null) {
            emptyText.setVisibility(View.GONE);
        }
    }

    private void applyFilter(String query) {
        if (!indexReady) {
            return;
        }

        String q = normalizeForSearch(query == null ? "" : query.trim());

        filteredSongs.clear();
        filteredArtists.clear();
        filteredAlbums.clear();

        if (q.isEmpty()) {
            toggleSections();
            return;
        }

        Map<String, CategoryItem> artistMap = new LinkedHashMap<>();
        Map<String, CategoryItem> albumMap = new LinkedHashMap<>();

        for (SongIndex row : indexedSongs) {
            if (!row.haystack.contains(q)) {
                continue;
            }

            if (filteredSongs.size() < MAX_SONG_RESULTS) {
                filteredSongs.add(row.song);
            }

            String artistName = safe(row.song.getArtist()).trim();
            if (!artistName.isEmpty() && artistMap.size() < MAX_CATEGORY_RESULTS) {
                String key = artistName.toLowerCase(Locale.ROOT);
                if (!artistMap.containsKey(key)) {
                    artistMap.put(key, new CategoryItem(
                            firstNonEmpty(row.song.getArtistId(), artistName),
                            artistName,
                            firstNonEmpty(row.song.getArtistImageUrl(), row.song.getArtworkUrl()),
                            "artist"
                    ));
                }
            }

            String albumName = safe(row.song.getAlbum()).trim();
            if (!albumName.isEmpty() && albumMap.size() < MAX_CATEGORY_RESULTS) {
                String key = albumName.toLowerCase(Locale.ROOT);
                if (!albumMap.containsKey(key)) {
                    albumMap.put(key, new CategoryItem(
                            firstNonEmpty(row.song.getPrimaryAlbumId(), albumName),
                            albumName,
                            firstNonEmpty(row.song.getAlbumImageUrl(), row.song.getArtworkUrl()),
                            "album"
                    ));
                }
            }

            if (filteredSongs.size() >= MAX_SONG_RESULTS
                    && artistMap.size() >= MAX_CATEGORY_RESULTS
                    && albumMap.size() >= MAX_CATEGORY_RESULTS) {
                break;
            }
        }

        filteredArtists.addAll(artistMap.values());
        filteredAlbums.addAll(albumMap.values());

        songAdapter.notifyDataSetChanged();
        artistAdapter.notifyDataSetChanged();
        albumAdapter.notifyDataSetChanged();

        toggleSections();
    }

    private void toggleSections() {
        boolean hasSongs = !filteredSongs.isEmpty();
        boolean hasArtists = !filteredArtists.isEmpty();
        boolean hasAlbums = !filteredAlbums.isEmpty();
        boolean hasAny = hasSongs || hasArtists || hasAlbums;
        boolean hasQuery = searchInput != null && !searchInput.getText().toString().trim().isEmpty();

        songsTitle.setVisibility(hasSongs ? View.VISIBLE : View.GONE);
        songsRecycler.setVisibility(hasSongs ? View.VISIBLE : View.GONE);

        artistsTitle.setVisibility(hasArtists ? View.VISIBLE : View.GONE);
        artistsRecycler.setVisibility(hasArtists ? View.VISIBLE : View.GONE);

        albumsTitle.setVisibility(hasAlbums ? View.VISIBLE : View.GONE);
        albumsRecycler.setVisibility(hasAlbums ? View.VISIBLE : View.GONE);

        emptyText.setVisibility((hasQuery && !hasAny) ? View.VISIBLE : View.GONE);
    }

    private void openCategoryPage(String type, CategoryItem item) {
        if (getContext() == null || item == null || item.getName().trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_TYPE, type);
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, item.getId());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_VALUE, item.getName().trim());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_IMAGE, item.getImageUrl());
        startActivity(intent);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeForSearch(String value) {
        if (value == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
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

    private static class SongIndex {
        final MusicFiles song;
        final String haystack;

        SongIndex(MusicFiles song, String haystack) {
            this.song = song;
            this.haystack = haystack;
        }
    }
}
