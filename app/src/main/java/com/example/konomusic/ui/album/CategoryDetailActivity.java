package com.example.konomusic.ui.album;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.data.firebase.FirebaseHelper;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.player.MusicAdapter;
import com.example.konomusic.ui.player.PlayerActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CategoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_TYPE = "extra_category_type";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_VALUE = "extra_category_value";
    public static final String EXTRA_CATEGORY_IMAGE = "extra_category_image";

    private RecyclerView recyclerView;
    private TextView subtitle;
    private TextView emptyText;
    private TextView title;
    private TextView totalPlays;
    private ImageView cover;
    private ImageView playButton;

    private FirebaseHelper firebaseHelper;
    private ArrayList<MusicFiles> songs = new ArrayList<>();
    private String selectedType;
    private String selectedCategoryId;
    private String selectedCategoryName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        ImageButton back = findViewById(R.id.categoryBack);
        recyclerView = findViewById(R.id.categoryRecycler);
        subtitle = findViewById(R.id.categorySubtitle);
        emptyText = findViewById(R.id.categoryEmpty);
        title = findViewById(R.id.categoryTitle);
        cover = findViewById(R.id.categoryCover);
        totalPlays = findViewById(R.id.categoryTotalPlays);
        playButton = findViewById(R.id.categoryPlayButton);

        back.setOnClickListener(v -> onBackPressed());
        playButton.setOnClickListener(v -> playFromCategory(0));

        String type = getIntent().getStringExtra(EXTRA_CATEGORY_TYPE);
        String categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String value = getIntent().getStringExtra(EXTRA_CATEGORY_VALUE);
        String imageUrl = getIntent().getStringExtra(EXTRA_CATEGORY_IMAGE);

        if (value == null) {
            value = "";
        }

        selectedType = type;
        selectedCategoryId = categoryId;
        selectedCategoryName = value;

        title.setText(value);
        subtitle.setText(buildSubtitle(type));

        Glide.with(this)
                .load((imageUrl == null || imageUrl.trim().isEmpty()) ? R.drawable.musicicon : imageUrl)
                .placeholder(R.drawable.musicicon)
                .error(R.drawable.musicicon)
                .centerCrop()
                .into(cover);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        firebaseHelper = new FirebaseHelper();
        loadSongs(type, categoryId, value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseHelper != null) {
            loadSongs(selectedType, selectedCategoryId, selectedCategoryName);
        }
    }

    private void loadSongs(String type, String categoryId, String categoryName) {
        firebaseHelper.loadSongsByCategory(type, categoryId, categoryName, new FirebaseHelper.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(ArrayList<MusicFiles> loadedSongs) {
                songs = loadedSongs == null ? new ArrayList<>() : loadedSongs;
                bindSongs(songs);
            }

            @Override
            public void onError(String errorMessage) {
                songs = new ArrayList<>();
                bindSongs(songs);
            }
        });
    }

    private void bindSongs(ArrayList<MusicFiles> songList) {
        recyclerView.setAdapter(new CategoryMusicAdapter(this, songList));

        boolean isEmpty = songList == null || songList.isEmpty();
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        playButton.setAlpha(isEmpty ? 0.45f : 1f);
        playButton.setEnabled(!isEmpty);

        long total = 0;
        if (songList != null) {
            for (MusicFiles song : songList) {
                if (song != null) {
                    total += song.getPlayCount();
                }
            }
        }
        totalPlays.setText("Tổng lượt nghe: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(total));
    }

    private void playFromCategory(int index) {
        if (songs == null || songs.isEmpty()) {
            Toast.makeText(this, "Danh sách đang trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (index < 0 || index >= songs.size()) {
            index = 0;
        }

        MusicAdapter.setCurrentList(songs);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("musicAdapter", "MusicAdapt");
        intent.putExtra("positionMfiles", index);
        startActivity(intent);
    }

    private String buildSubtitle(String type) {
        if ("album".equalsIgnoreCase(type)) {
            return "Album";
        }
        if ("genre".equalsIgnoreCase(type)) {
            return "Thể loại";
        }
        if ("artist".equalsIgnoreCase(type)) {
            return "Nghệ sĩ";
        }
        return "Danh sách bài hát";
    }
}
