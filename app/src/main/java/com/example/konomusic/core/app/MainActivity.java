package com.example.konomusic.core.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.konomusic.R;
import com.example.konomusic.data.firebase.FirebaseHelper;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.album.AlbumFragment;
import com.example.konomusic.ui.home.SongsFragment;
import com.example.konomusic.ui.library.LibraryFragment;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.NowPlayingFragmentBottom;
import com.example.konomusic.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static ArrayList<MusicFiles> musicFiles;
    public static boolean shuffleBoolean = false, repeatBoolean = false;
    public static ArrayList<MusicFiles> albums = new ArrayList<>();
    private String MY_SORT_PREF = "SortOrder";
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String ARTIST_TO_FRAG = null;
    public static String SONG_NAME_TO_FRAG = null;
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    private FirebaseHelper firebaseHelper;

    // Thêm static instance để fragment có thể truy cập
    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    public static ArrayList<MusicFiles> getMusicFiles() {
        return musicFiles;
    }

    public static ArrayList<MusicFiles> getAlbums() {
        return albums;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseApp.initializeApp(this);
        firebaseHelper = new FirebaseHelper();

        // Chi su dung du lieu Firebase: khoi tao list rong roi init UI.
        musicFiles = new ArrayList<>();
        initViewPager();

        // Ẩn tạm container, fragment sẽ tự hiển thị nếu có state phát nhạc hợp lệ.
        View miniPlayerContainer = findViewById(R.id.frag_bottom_player);
        if (miniPlayerContainer != null) {
            miniPlayerContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Không cần gì
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE, null);
        String artist = preferences.getString(ARTIST_NAME, null);
        String song_name = preferences.getString(SONG_NAME, null);

        if (path != null && !path.isEmpty()) {
            SHOW_MINI_PLAYER = true;
            PATH_TO_FRAG = path;
            ARTIST_TO_FRAG = artist;
            SONG_NAME_TO_FRAG = song_name;
        }
        else{
            SHOW_MINI_PLAYER = false;
            PATH_TO_FRAG = null;
            ARTIST_TO_FRAG = null;
            SONG_NAME_TO_FRAG = null;
        }

        // Force mini-player UI to resync every time MainActivity resumes.
        NowPlayingFragmentBottom.refreshFromState();
        LibraryFragment.refreshContent();
    }


    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Trang chủ");
        viewPagerAdapter.addFragments(new SearchFragment(), "Tìm kiếm");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Album");
        viewPagerAdapter.addFragments(new LibraryFragment(), "Thư viện");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_search) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_album) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_library) {
                viewPager.setCurrentItem(3, false);
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_home);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else if (position == 1) {
                    bottomNav.setSelectedItemId(R.id.nav_search);
                } else if (position == 2) {
                    bottomNav.setSelectedItemId(R.id.nav_album);
                } else if (position == 3) {
                    bottomNav.setSelectedItemId(R.id.nav_library);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Load danh sach bai hat tu Firebase.
        loadFirebaseSongs();
    }

    // Load danh sách bài từ Firebase
    private void loadFirebaseSongs() {
        if (firebaseHelper != null) {
            firebaseHelper.loadSongsFromFirebase(new FirebaseHelper.OnSongsLoadedListener() {
                @Override
                public void onSongsLoaded(ArrayList<MusicFiles> firebaseSongs) {
                    if (firebaseSongs == null) {
                        return;
                    }

                    if (musicFiles == null) {
                        musicFiles = new ArrayList<>();
                    }

                    ArrayList<MusicFiles> onlyFirebase = new ArrayList<>();
                    Set<String> seen = new HashSet<>();
                    for (MusicFiles song : firebaseSongs) {
                        String key = buildSongKey(song);
                        if (key == null || seen.contains(key)) {
                            continue;
                        }
                        onlyFirebase.add(song);
                        seen.add(key);
                    }

                    musicFiles.clear();
                    musicFiles.addAll(onlyFirebase);
                    Log.d("MainActivity", "Loaded " + onlyFirebase.size() + " songs from Firebase.");

                    MusicAdapter homeAdapter = SongsFragment.getMusicAdapter();
                    if (homeAdapter != null) {
                        homeAdapter.updateList(musicFiles);
                        homeAdapter.notifyDataSetChanged();
                    }
                    SongsFragment.refreshHomeContent();
                    SearchFragment.refreshSearchContent();
                    LibraryFragment.refreshContent();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("MainActivity", "Firebase error: " + errorMessage);
                    Toast.makeText(MainActivity.this, "Cannot load Firebase songs", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String buildSongKey(MusicFiles song) {
        if (song == null) {
            return null;
        }
        if (song.getId() != null && !song.getId().trim().isEmpty()) {
            return "id:" + song.getId().trim();
        }
        if (song.getStreamUrl() != null && !song.getStreamUrl().trim().isEmpty()) {
            return "url:" + song.getStreamUrl().trim();
        }
        if (song.getPath() != null && !song.getPath().trim().isEmpty()) {
            return "path:" + song.getPath().trim();
        }
        return null;
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
//        public ViewPagerAdapter(@NonNull @org.jetbrains.annotations.NotNull FragmentManager fm) {
        public ViewPagerAdapter(@NonNull @org.jetbrains.annotations.NotNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @org.jetbrains.annotations.NotNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for (MusicFiles song : musicFiles){
            if (song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        MusicAdapter homeAdapter = SongsFragment.getMusicAdapter();
        if (homeAdapter != null) {
            homeAdapter.updateList(myFiles);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        int itemId = item.getItemId();

        if (itemId == R.id.by_title) {
            editor.putString("sorting", "sortByTitle");
            editor.apply();
            recreate();
            return true;
        } else if (itemId == R.id.by_date) {
            editor.putString("sorting", "sortByDate");
            editor.apply();
            recreate();
            return true;
        } else if (itemId == R.id.by_size) {
            editor.putString("sorting", "sortBySize");
            editor.apply();
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}