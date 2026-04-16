package com.example.konomusic.ui.album;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.data.firebase.FirebaseHelper;
import com.example.konomusic.domain.model.CategoryItem;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.home.HomeTagAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlbumFragment extends Fragment {

    private RecyclerView topRecycler;
    private RecyclerView recommendedRecycler;
    private RecyclerView albumListRecycler;
    private TextView emptyText;

    private FirebaseHelper firebaseHelper;
    private ArrayList<CategoryItem> remoteAlbumItems = new ArrayList<>();

    private long recommendationSeed = System.currentTimeMillis();
    private String recommendationCacheKey = "";
    private ArrayList<CategoryItem> cachedRecommendations = new ArrayList<>();

    public AlbumFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        topRecycler = view.findViewById(R.id.albumTopRecycler);
        recommendedRecycler = view.findViewById(R.id.albumRecommendedRecycler);
        albumListRecycler = view.findViewById(R.id.albumListRecycler);
        emptyText = view.findViewById(R.id.albumEmptyText);

        topRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recommendedRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        albumListRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        recommendationSeed = System.currentTimeMillis();
        recommendationCacheKey = "";
        cachedRecommendations = new ArrayList<>();

        firebaseHelper = new FirebaseHelper();
        setupAlbumSections();
        loadRemoteAlbums();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        recommendationSeed = System.nanoTime();
        recommendationCacheKey = "";
        loadRemoteAlbums();
    }

    private void loadRemoteAlbums() {
        if (firebaseHelper == null) {
            return;
        }
        firebaseHelper.loadCategoryItems("album", new FirebaseHelper.OnCategoryItemsLoadedListener() {
            @Override
            public void onLoaded(ArrayList<CategoryItem> items) {
                remoteAlbumItems = items == null ? new ArrayList<>() : items;
                setupAlbumSections();
            }

            @Override
            public void onError(String errorMessage) {
                remoteAlbumItems = new ArrayList<>();
                setupAlbumSections();
            }
        });
    }

    private void setupAlbumSections() {
        ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
        ArrayList<MusicFiles> source = mainSongs == null
                ? new ArrayList<>()
                : new ArrayList<>(mainSongs);

        ArrayList<CategoryItem> rankedAlbums = buildRankedAlbums(source);
        ArrayList<CategoryItem> topAlbums = take(rankedAlbums, 10);
        ArrayList<CategoryItem> recommendations = buildRecommendations(rankedAlbums, topAlbums, 10);

        topRecycler.setAdapter(new HomeTagAdapter(topAlbums, this::openCategoryPage));
        recommendedRecycler.setAdapter(new HomeTagAdapter(recommendations, this::openCategoryPage));

        ArrayList<MusicFiles> albumRows = buildAlbumRows(source);
        albumListRecycler.setAdapter(new AlbumAdapter(getContext(), albumRows));

        boolean isEmpty = albumRows.isEmpty();
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        albumListRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private ArrayList<CategoryItem> buildRankedAlbums(ArrayList<MusicFiles> songs) {
        RemoteAlbumIndex remoteIndex = buildRemoteAlbumIndex();

        Map<String, AlbumStat> stats = new LinkedHashMap<>();
        for (MusicFiles song : songs) {
            if (song == null || song.getAlbum() == null || song.getAlbum().trim().isEmpty()) {
                continue;
            }
            CategoryItem remote = remoteIndex.resolve(song);
            if (!remoteIndex.isEmpty() && remote == null) {
                continue;
            }

            String key = remote != null
                    ? canonicalAlbumKey(remote)
                    : canonicalAlbumNameKey(song.getAlbum());
            if (key.isEmpty()) {
                continue;
            }

            String albumName = remote != null && remote.getName() != null && !remote.getName().trim().isEmpty()
                    ? remote.getName().trim()
                    : song.getAlbum().trim();
            AlbumStat stat = stats.get(key);
            if (stat == null) {
                stat = new AlbumStat();
                stat.name = albumName;
                stat.id = firstNonEmpty(
                        remote != null ? remote.getId() : "",
                        song.getPrimaryAlbumId(),
                        albumName
                );
                stat.imageUrl = firstNonEmpty(
                        remote != null ? remote.getImageUrl() : "",
                        song.getAlbumImageUrl(),
                        song.getArtworkUrl()
                );
                stats.put(key, stat);
            }
            stat.playCount += song.getPlayCount();
            if (stat.id.isEmpty()) {
                stat.id = firstNonEmpty(remote != null ? remote.getId() : "", song.getPrimaryAlbumId());
            }
            if (stat.imageUrl.isEmpty()) {
                stat.imageUrl = firstNonEmpty(
                        remote != null ? remote.getImageUrl() : "",
                        song.getAlbumImageUrl(),
                        song.getArtworkUrl()
                );
            }
        }

        for (CategoryItem item : remoteAlbumItems) {
            String key = canonicalAlbumKey(item);
            if (key.isEmpty()) {
                continue;
            }
            if (!stats.containsKey(key)) {
                AlbumStat stat = new AlbumStat();
                stat.name = item.getName().trim();
                stat.id = firstNonEmpty(item.getId());
                stat.imageUrl = firstNonEmpty(item.getImageUrl());
                stat.playCount = 0L;
                stats.put(key, stat);
            }
        }

        ArrayList<Map.Entry<String, AlbumStat>> rankedEntries = new ArrayList<>(stats.entrySet());
        Collections.sort(rankedEntries, (left, right) -> {
            int cmp = Long.compare(right.getValue().playCount, left.getValue().playCount);
            if (cmp != 0) {
                return cmp;
            }
            return left.getValue().name.compareToIgnoreCase(right.getValue().name);
        });

        ArrayList<CategoryItem> out = new ArrayList<>();
        for (Map.Entry<String, AlbumStat> entry : rankedEntries) {
            AlbumStat stat = entry.getValue();
            CategoryItem remote = remoteIndex.byCanonical.get(entry.getKey());
            String albumId = firstNonEmpty(remote != null ? remote.getId() : "", stat.id, stat.name);
            String imageUrl = firstNonEmpty(remote != null ? remote.getImageUrl() : "", stat.imageUrl);
            out.add(new CategoryItem(albumId, stat.name, imageUrl, "album"));
        }

        if (!rankedEntries.isEmpty()) {
            long maxPlayCount = rankedEntries.get(0).getValue().playCount;
            if (maxPlayCount == 0L) {
                Collections.shuffle(out);
            }
        }
        return out;
    }

    private ArrayList<CategoryItem> buildRecommendations(ArrayList<CategoryItem> ranked,
                                                         ArrayList<CategoryItem> top,
                                                         int limit) {
        ArrayList<CategoryItem> out = new ArrayList<>();
        if (ranked == null || ranked.isEmpty() || limit <= 0) {
            return out;
        }

        String cacheKey = buildRecommendationCacheKey(ranked, top, limit);
        if (cacheKey.equals(recommendationCacheKey) && !cachedRecommendations.isEmpty()) {
            return take(cachedRecommendations, limit);
        }

        LinkedHashSet<String> excludeTopKeys = new LinkedHashSet<>();
        if (top != null) {
            for (CategoryItem item : top) {
                if (item != null && item.getName() != null) {
                    excludeTopKeys.add(item.getName().trim().toLowerCase());
                }
            }
        }

        LinkedHashSet<String> pickedKeys = new LinkedHashSet<>();

        ArrayList<CategoryItem> pool = new ArrayList<>();
        for (CategoryItem item : ranked) {
            if (item == null || item.getName() == null) {
                continue;
            }
            String key = item.getName().trim().toLowerCase();
            if (!excludeTopKeys.contains(key)) {
                pool.add(item);
            }
        }

        Collections.shuffle(pool, new Random(recommendationSeed));
        for (CategoryItem item : pool) {
            String key = item.getName().trim().toLowerCase();
            if (pickedKeys.contains(key)) {
                continue;
            }
            out.add(item);
            pickedKeys.add(key);
            if (out.size() >= limit) {
                break;
            }
        }

        if (out.size() < limit) {
            ArrayList<CategoryItem> fallbackPool = new ArrayList<>(ranked);
            Collections.shuffle(fallbackPool, new Random(recommendationSeed ^ 0x9E3779B97F4A7C15L));
            for (CategoryItem item : fallbackPool) {
                if (item == null || item.getName() == null) {
                    continue;
                }
                String key = item.getName().trim().toLowerCase();
                if (pickedKeys.contains(key)) {
                    continue;
                }
                out.add(item);
                pickedKeys.add(key);
                if (out.size() >= limit) {
                    break;
                }
            }
        }

        recommendationCacheKey = cacheKey;
        cachedRecommendations = new ArrayList<>(out);
        return out;
    }

    private String buildRecommendationCacheKey(ArrayList<CategoryItem> ranked,
                                               ArrayList<CategoryItem> top,
                                               int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append(limit).append('|').append(recommendationSeed).append('|');
        sb.append(ranked == null ? 0 : ranked.size()).append('|');
        sb.append(top == null ? 0 : top.size());

        if (ranked != null) {
            for (CategoryItem item : ranked) {
                if (item == null) {
                    continue;
                }
                sb.append('|').append(item.getId() == null ? "" : item.getId().trim())
                        .append('#').append(item.getName() == null ? "" : item.getName().trim());
            }
        }
        return sb.toString();
    }

    private ArrayList<MusicFiles> buildAlbumRows(ArrayList<MusicFiles> songs) {
        RemoteAlbumIndex remoteIndex = buildRemoteAlbumIndex();
        Map<String, MusicFiles> bestByAlbum = new LinkedHashMap<>();

        if (songs != null) {
            for (MusicFiles song : songs) {
                mergeBestAlbumCandidate(bestByAlbum, song, remoteIndex);
            }
        }

        ArrayList<MusicFiles> mainAlbums = MainActivity.getAlbums();
        if (mainAlbums != null) {
            for (MusicFiles albumSong : mainAlbums) {
                mergeBestAlbumCandidate(bestByAlbum, albumSong, remoteIndex);
            }
        }

        if (remoteIndex.isEmpty()) {
            return new ArrayList<>(bestByAlbum.values());
        }

        ArrayList<MusicFiles> out = new ArrayList<>();
        for (CategoryItem remote : remoteAlbumItems) {
            if (remote == null || remote.getName() == null || remote.getName().trim().isEmpty()) {
                continue;
            }
            String key = canonicalAlbumKey(remote);
            if (key.isEmpty()) {
                continue;
            }
            MusicFiles candidate = bestByAlbum.get(key);
            if (candidate != null) {
                if (firstNonEmpty(candidate.getAlbumImageUrl()).isEmpty()) {
                    candidate.setAlbumImageUrl(firstNonEmpty(remote.getImageUrl(), candidate.getArtworkUrl()));
                }
                if (firstNonEmpty(candidate.getPrimaryAlbumId()).isEmpty()) {
                    candidate.setAlbumId(firstNonEmpty(remote.getId()));
                }
                out.add(candidate);
                continue;
            }

            MusicFiles placeholder = new MusicFiles();
            placeholder.setAlbum(remote.getName().trim());
            placeholder.setArtist("Unknown Artist");
            placeholder.setAlbumId(firstNonEmpty(remote.getId()));
            placeholder.setAlbumImageUrl(firstNonEmpty(remote.getImageUrl()));
            placeholder.setArtworkUrl(firstNonEmpty(remote.getImageUrl()));
            placeholder.setFromFirebase(true);
            out.add(placeholder);
        }
        return out;
    }

    private RemoteAlbumIndex buildRemoteAlbumIndex() {
        RemoteAlbumIndex out = new RemoteAlbumIndex();
        for (CategoryItem item : remoteAlbumItems) {
            if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
                continue;
            }
            String nameKey = canonicalAlbumNameKey(item.getName());
            String id = firstNonEmpty(item.getId());
            String canonicalKey = canonicalAlbumKey(item);

            if (!nameKey.isEmpty()) {
                out.byName.put(nameKey, item);
            }
            if (!id.isEmpty()) {
                out.byId.put(id, item);
            }
            if (!canonicalKey.isEmpty()) {
                out.byCanonical.put(canonicalKey, item);
            }
        }
        return out;
    }

    private void mergeBestAlbumCandidate(Map<String, MusicFiles> bestByAlbum,
                                         MusicFiles candidate,
                                         RemoteAlbumIndex remoteIndex) {
        if (candidate == null || candidate.getAlbum() == null || candidate.getAlbum().trim().isEmpty()) {
            return;
        }

        String key = resolveAlbumKey(candidate, remoteIndex);
        if (key.isEmpty()) {
            return;
        }
        MusicFiles current = bestByAlbum.get(key);
        if (current == null) {
            bestByAlbum.put(key, candidate);
            return;
        }

        int currentScore = scoreAlbumVisual(current);
        int candidateScore = scoreAlbumVisual(candidate);
        if (candidateScore > currentScore) {
            bestByAlbum.put(key, candidate);
        }
    }

    private String resolveAlbumKey(MusicFiles song, RemoteAlbumIndex remoteIndex) {
        if (song == null) {
            return "";
        }
        if (remoteIndex != null && !remoteIndex.isEmpty()) {
            CategoryItem remote = remoteIndex.resolve(song);
            return canonicalAlbumKey(remote);
        }
        return canonicalAlbumNameKey(song.getAlbum());
    }

    private String canonicalAlbumKey(CategoryItem item) {
        if (item == null) {
            return "";
        }
        String id = firstNonEmpty(item.getId());
        if (!id.isEmpty()) {
            return "id:" + id;
        }
        return canonicalAlbumNameKey(item.getName());
    }

    private String canonicalAlbumNameKey(String name) {
        String value = firstNonEmpty(name);
        return value.isEmpty() ? "" : "name:" + value.toLowerCase();
    }

    private int scoreAlbumVisual(MusicFiles item) {
        int score = 0;
        if (!firstNonEmpty(item.getAlbumImageUrl()).isEmpty()) {
            score += 4;
        }
        if (!firstNonEmpty(item.getArtworkUrl()).isEmpty()) {
            score += 3;
        }
        if (item.isFromFirebase()) {
            score += 2;
        }
        if (!firstNonEmpty(item.getPath()).isEmpty()) {
            score += 1;
        }
        return score;
    }

    private ArrayList<CategoryItem> take(List<CategoryItem> source, int limit) {
        ArrayList<CategoryItem> out = new ArrayList<>();
        int max = Math.min(limit, source.size());
        for (int i = 0; i < max; i++) {
            out.add(source.get(i));
        }
        return out;
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

    private void openCategoryPage(CategoryItem item) {
        if (getContext() == null || item == null || item.getName().trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_TYPE, "album");
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, item.getId());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_VALUE, item.getName().trim());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_IMAGE, item.getImageUrl());
        startActivity(intent);
    }

    private static class AlbumStat {
        String id = "";
        String name = "";
        String imageUrl = "";
        long playCount = 0L;
    }

    private static class RemoteAlbumIndex {
        final Map<String, CategoryItem> byId = new HashMap<>();
        final Map<String, CategoryItem> byName = new HashMap<>();
        final Map<String, CategoryItem> byCanonical = new HashMap<>();

        boolean isEmpty() {
            return byId.isEmpty() && byName.isEmpty();
        }

        CategoryItem resolve(MusicFiles song) {
            if (song == null) {
                return null;
            }
            String albumId = song.getPrimaryAlbumId() == null ? "" : song.getPrimaryAlbumId().trim();
            if (!albumId.isEmpty()) {
                CategoryItem byAlbumId = byId.get(albumId);
                if (byAlbumId != null) {
                    return byAlbumId;
                }
            }
            String albumName = song.getAlbum() == null ? "" : song.getAlbum().trim().toLowerCase();
            if (albumName.isEmpty()) {
                return null;
            }
            return byName.get("name:" + albumName);
        }
    }
}
