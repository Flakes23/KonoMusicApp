package com.example.konomusic.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.auth.AuthManager;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.data.firebase.FirebaseHelper;
import com.example.konomusic.domain.model.CategoryItem;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.album.CategoryDetailActivity;
import com.example.konomusic.ui.library.LibraryFragment;
import com.example.konomusic.ui.player.MusicAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;

public class SongsFragment extends Fragment {

    private static SongsFragment instance;

    RecyclerView recyclerView;
    RecyclerView albumsRecycler;
    RecyclerView genresRecycler;
    RecyclerView topRecycler;
    RecyclerView recommendedRecycler;
    RecyclerView artistsRecycler;
    View homeWelcomeRow;
    View homeGuestBox;
    TextView homeWelcomeText;
    TextView homeWelcomeAvatar;
    MaterialButton homeLogoutBtn;
    MaterialButton homeLoginBtn;

    static MusicAdapter musicAdapter;

    private FirebaseHelper firebaseHelper;
    private ArrayList<CategoryItem> albumItems = new ArrayList<>();
    private ArrayList<CategoryItem> genreItems = new ArrayList<>();
    private ArrayList<CategoryItem> artistItems = new ArrayList<>();

    private long recommendationSeed = System.currentTimeMillis();
    private String recommendationCacheKey = "";
    private ArrayList<MusicFiles> cachedRecommendations = new ArrayList<>();

    public SongsFragment() {
    }

    public static void refreshHomeContent() {
        if (instance != null) {
            instance.setupSections();
            instance.showAllSongs();
            instance.updateWelcomeHeader();
        }
    }

    public static MusicAdapter getMusicAdapter() {
        return musicAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        instance = this;

        recyclerView = view.findViewById(R.id.recyclerView);
        albumsRecycler = view.findViewById(R.id.homeAlbumsRecycler);
        genresRecycler = view.findViewById(R.id.homeGenresRecycler);
        topRecycler = view.findViewById(R.id.homeTopRecycler);
        recommendedRecycler = view.findViewById(R.id.homeRecommendedRecycler);
        artistsRecycler = view.findViewById(R.id.homeArtistsRecycler);
        homeWelcomeRow = view.findViewById(R.id.homeWelcomeRow);
        homeGuestBox = view.findViewById(R.id.homeGuestBox);
        homeWelcomeText = view.findViewById(R.id.homeWelcomeText);
        homeWelcomeAvatar = view.findViewById(R.id.homeWelcomeAvatar);
        homeLogoutBtn = view.findViewById(R.id.homeLogoutBtn);
        homeLoginBtn = view.findViewById(R.id.homeLoginBtn);

        firebaseHelper = new FirebaseHelper();

        homeLogoutBtn.setOnClickListener(v -> {
            AuthManager.signOut();
            updateWelcomeHeader();
            LibraryFragment.refreshContent();
        });

        homeLoginBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                AuthManager.openLogin(getActivity());
            }
        });

        recommendationSeed = System.currentTimeMillis();
        recommendationCacheKey = "";
        cachedRecommendations = new ArrayList<>();

        setupAllSongs();
        setupSections();
        showAllSongs();
        updateWelcomeHeader();
        loadCategories();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Moi lan quay lai Home, tao bo goi y random moi.
        recommendationSeed = System.nanoTime();
        recommendationCacheKey = "";
        if (isAdded()) {
            setupSections();
            updateWelcomeHeader();
        }
    }

    private void updateWelcomeHeader() {
        if (!isAdded() || homeWelcomeRow == null || homeGuestBox == null || homeWelcomeText == null || homeWelcomeAvatar == null) {
            return;
        }

        FirebaseUser user = AuthManager.currentUser();
        if (user == null) {
            homeWelcomeRow.setVisibility(View.GONE);
            homeGuestBox.setVisibility(View.VISIBLE);
            return;
        }

        String displayName = resolveDisplayName(user);
        homeWelcomeText.setText(getString(R.string.home_welcome_user, displayName));
        homeWelcomeAvatar.setText(resolveAvatarInitial(displayName));
        homeGuestBox.setVisibility(View.GONE);
        homeWelcomeRow.setVisibility(View.VISIBLE);
    }

    private String resolveDisplayName(FirebaseUser user) {
        if (user == null) {
            return "Ban";
        }
        String name = user.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        String email = user.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            String trimmed = email.trim();
            int at = trimmed.indexOf('@');
            return at > 0 ? trimmed.substring(0, at) : trimmed;
        }
        return "Ban";
    }

    private String resolveAvatarInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "B";
        }
        String clean = name.trim();
        return clean.substring(0, 1).toUpperCase(Locale.getDefault());
    }

    private void setupAllSongs() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        musicAdapter = new MusicAdapter(getContext(), MainActivity.getMusicFiles());
        recyclerView.setAdapter(musicAdapter);
    }

    private void showAllSongs() {
        if (musicAdapter == null) {
            return;
        }
        ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
        ArrayList<MusicFiles> source = mainSongs == null ? new ArrayList<>() : new ArrayList<>(mainSongs);
        musicAdapter.updateList(source);
    }

    private void setupSections() {
        ArrayList<MusicFiles> mainSongs = MainActivity.getMusicFiles();
        ArrayList<MusicFiles> source = mainSongs == null ? new ArrayList<>() : new ArrayList<>(mainSongs);

        albumsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        albumsRecycler.setAdapter(new HomeTagAdapter(resolveCategoryItems("album", source), this::openCategoryPage));

        genresRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        genresRecycler.setAdapter(new HomeTagAdapter(resolveCategoryItems("genre", source), this::openCategoryPage));

        topRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        topRecycler.setAdapter(new HomeSongAdapter(getContext(), buildTopSongs(source, 12)));

        recommendedRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recommendedRecycler.setAdapter(new HomeSongAdapter(getContext(), buildRecommendations(source, 12)));

        artistsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        artistsRecycler.setAdapter(new HomeTagAdapter(resolveCategoryItems("artist", source), this::openCategoryPage));
    }

    private void loadCategories() {
        if (firebaseHelper == null) {
            return;
        }

        firebaseHelper.loadCategoryItems("album", new FirebaseHelper.OnCategoryItemsLoadedListener() {
            @Override
            public void onLoaded(ArrayList<CategoryItem> items) {
                albumItems = items == null ? new ArrayList<>() : items;
                setupSections();
            }

            @Override
            public void onError(String errorMessage) {
                albumItems = new ArrayList<>();
            }
        });

        firebaseHelper.loadCategoryItems("genre", new FirebaseHelper.OnCategoryItemsLoadedListener() {
            @Override
            public void onLoaded(ArrayList<CategoryItem> items) {
                genreItems = items == null ? new ArrayList<>() : items;
                setupSections();
            }

            @Override
            public void onError(String errorMessage) {
                genreItems = new ArrayList<>();
            }
        });

        firebaseHelper.loadCategoryItems("artist", new FirebaseHelper.OnCategoryItemsLoadedListener() {
            @Override
            public void onLoaded(ArrayList<CategoryItem> items) {
                artistItems = items == null ? new ArrayList<>() : items;
                setupSections();
            }

            @Override
            public void onError(String errorMessage) {
                artistItems = new ArrayList<>();
            }
        });
    }

    private ArrayList<CategoryItem> resolveCategoryItems(String type, ArrayList<MusicFiles> source) {
        ArrayList<CategoryItem> fromFirestore;
        if ("album".equalsIgnoreCase(type)) {
            fromFirestore = albumItems;
        } else if ("genre".equalsIgnoreCase(type)) {
            fromFirestore = genreItems;
        } else {
            fromFirestore = artistItems;
        }

        if (fromFirestore != null && !fromFirestore.isEmpty()) {
            return fromFirestore;
        }

        LinkedHashSet<String> fallback = new LinkedHashSet<>();
        for (MusicFiles song : source) {
            if (song == null) {
                continue;
            }
            String value;
            String image;
            String id;
            if ("album".equalsIgnoreCase(type)) {
                value = song.getAlbum();
                image = song.getAlbumImageUrl();
                id = song.getPrimaryAlbumId();
            } else if ("genre".equalsIgnoreCase(type)) {
                value = song.getGenre();
                image = song.getGenreImageUrl();
                id = song.getGenreId();
            } else {
                value = song.getArtist();
                image = song.getArtistImageUrl();
                id = song.getArtistId();
            }
            if (value == null || value.trim().isEmpty() || fallback.contains(value.trim())) {
                continue;
            }
            fallback.add(value.trim());
            if (fromFirestore == null) {
                fromFirestore = new ArrayList<>();
            }
            fromFirestore.add(new CategoryItem(id, value.trim(), firstNonEmpty(image, song.getArtworkUrl()), type));
        }
        return fromFirestore == null ? new ArrayList<>() : fromFirestore;
    }

    private String firstNonEmpty(String left, String right) {
        if (left != null && !left.trim().isEmpty()) {
            return left.trim();
        }
        if (right != null && !right.trim().isEmpty()) {
            return right.trim();
        }
        return "";
    }

    private void openCategoryPage(CategoryItem item) {
        if (getContext() == null || item == null || item.getName().trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(getContext(), CategoryDetailActivity.class);
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_TYPE, item.getType());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, item.getId());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_VALUE, item.getName().trim());
        intent.putExtra(CategoryDetailActivity.EXTRA_CATEGORY_IMAGE, item.getImageUrl());
        startActivity(intent);
    }


    private ArrayList<MusicFiles> buildTopSongs(ArrayList<MusicFiles> source, int limit) {
        ArrayList<MusicFiles> sorted = new ArrayList<>(source);
        Collections.sort(sorted, (a, b) -> Long.compare(
                b != null ? b.getPlayCount() : 0L,
                a != null ? a.getPlayCount() : 0L
        ));

        if (!sorted.isEmpty() && sorted.get(0).getPlayCount() == 0L) {
            Collections.shuffle(sorted);
        }

        return take(sorted, limit);
    }

    private ArrayList<MusicFiles> buildRecommendations(ArrayList<MusicFiles> source, int limit) {
        ArrayList<MusicFiles> rec = new ArrayList<>();
        if (source == null || source.isEmpty() || limit <= 0) {
            return rec;
        }

        String cacheKey = buildRecommendationCacheKey(source, limit);
        if (cacheKey.equals(recommendationCacheKey) && !cachedRecommendations.isEmpty()) {
            return take(new ArrayList<>(cachedRecommendations), limit);
        }

        ArrayList<MusicFiles> pool = new ArrayList<>(source);
        Collections.shuffle(pool, new Random(recommendationSeed));
        rec = take(pool, limit);

        recommendationCacheKey = cacheKey;
        cachedRecommendations = new ArrayList<>(rec);
        return rec;
    }

    private String buildRecommendationCacheKey(ArrayList<MusicFiles> source, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append(limit).append('|').append(recommendationSeed).append('|').append(source.size());
        for (MusicFiles song : source) {
            if (song == null) {
                continue;
            }
            sb.append('|');
            String id = song.getId();
            if (id != null && !id.trim().isEmpty()) {
                sb.append(id.trim());
            } else if (song.getPath() != null) {
                sb.append(song.getPath().trim());
            } else {
                sb.append(song.getTitle() == null ? "" : song.getTitle().trim());
            }
        }
        return sb.toString();
    }

    private ArrayList<MusicFiles> take(ArrayList<MusicFiles> source, int limit) {
        ArrayList<MusicFiles> out = new ArrayList<>();
        int max = Math.min(limit, source.size());
        for (int i = 0; i < max; i++) {
            out.add(source.get(i));
        }
        return out;
    }
}
