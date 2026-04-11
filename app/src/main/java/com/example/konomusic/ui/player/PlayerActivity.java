package com.example.konomusic.ui.player;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.palette.graphics.Palette;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.InputType;
import android.text.Layout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.playback.ActionPlaying;
import com.example.konomusic.playback.MusicService;
import com.example.konomusic.ui.library.PlaylistDialogUi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Random;

import static com.example.konomusic.core.app.ApplicationClass.ACTION_NEXT;
import static com.example.konomusic.core.app.ApplicationClass.ACTION_PLAY;
import static com.example.konomusic.core.app.ApplicationClass.ACTION_PREVIOUS;
import static com.example.konomusic.core.app.ApplicationClass.CHANNEL_ID_2;
import static com.example.konomusic.core.app.MainActivity.PATH_TO_FRAG;
import static com.example.konomusic.core.app.MainActivity.musicFiles;
import static com.example.konomusic.core.app.MainActivity.repeatBoolean;
import static com.example.konomusic.core.app.MainActivity.shuffleBoolean;
import static com.example.konomusic.ui.player.MusicAdapter.mFiles;
import static com.example.konomusic.playback.MusicService.passPosition;
import static java.security.AccessController.getContext;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    private static final int PENDING_NONE = 0;
    private static final int PENDING_FAVORITE = 1;
    private static final int PENDING_ADD_TO_PLAYLIST = 2;
    private static final String SONG_POSITION_PREFIX = "SONG_POS_";
    public static final String EXTRA_ATTACH_ONLY = "extra_attach_only_current_session";

    TextView song_name, artist_name, duration_played, duration_total, album_name;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    ImageView blurBackground;
    private ImageView favoriteBtn;
    private ImageView addPlaylistBtn;
    private com.example.konomusic.data.repository.UserLibraryRepository libraryRepository;
    private boolean isCurrentFavorite = false;
    private int pendingAuthAction = PENDING_NONE;
    static byte[] artist_image;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;

    int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
//    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            syncSeekBarWithPlayer();
            handler.postDelayed(this, 1000);
        }
    };
    MusicService musicService;
    static MusicService passMusicService;
    static GradientDrawable gradientDrawableBg;
    private boolean isServiceBound = false;
    private boolean isAttachOnlyMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("playerActivitypass", true);
        editor.commit();
        initViews();
        libraryRepository = new com.example.konomusic.data.repository.UserLibraryRepository();
        getIntenMethod();
//        PassingBottomFrag();
        updateModeButtonsUi();
        if (shuffleBoolean && !repeatBoolean){
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
        }else if (!shuffleBoolean && repeatBoolean){
            repeatBtn.setImageResource(R.drawable.ic_repeat_on);
        }else if (shuffleBoolean && repeatBoolean){
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            repeatBtn.setImageResource(R.drawable.ic_repeat_on);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser){
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseBtnClicked();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtnClicked();
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevBtnClicked();
            }
        });

        startSeekbarUpdates();
        //        PlayerActivity.this.runOnUiThread(new Runnable() {
        //            @Override
        //            public void run() {
        //                if (musicService != null){
        //                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
        //                    seekBar.setProgress(mCurrentPosition);
        //                    duration_played.setText(formattedTime(mCurrentPosition));
        //                }
        //                handler.postDelayed(this, 1000);
        //            }
        //        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleBoolean = !shuffleBoolean;
                updateModeButtonsUi();
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatBoolean = !repeatBoolean;
                updateModeButtonsUi();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent_D = new Intent(getApplicationContext(), MainActivity.class);
//                intent_D.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent_D);
                finish();
            }
        });

        favoriteBtn.setOnClickListener(v -> onFavoriteClicked());
        addPlaylistBtn.setOnClickListener(v -> onAddPlaylistClicked());
    }

    @Override
    public void onBackPressed() {
        finish();
//        finish();
//        overridePendingTransition( 0, 0);
//        startActivity(getIntent());
//        overridePendingTransition( 0, 0);
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        isServiceBound = bindService(intent, this, BIND_AUTO_CREATE);
        startSeekbarUpdates();
        refreshFavoriteState();
        updateModeButtonsUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != com.example.konomusic.auth.AuthManager.LOGIN_REQUEST_CODE) {
            return;
        }

        if (resultCode != RESULT_OK) {
            pendingAuthAction = PENDING_NONE;
            return;
        }

        int action = pendingAuthAction;
        pendingAuthAction = PENDING_NONE;
        if (action == PENDING_FAVORITE) {
            onFavoriteClicked();
        } else if (action == PENDING_ADD_TO_PLAYLIST) {
            onAddPlaylistClicked();
        }
    }

    @Override
    protected void onPause() {
        stopSeekbarUpdates();
        super.onPause();
        if (isServiceBound) {
            if (musicService != null) {
                musicService.persistPositionNow();
                musicService.setCallBack(null);
            }
            unbindService(this);
            isServiceBound = false;
        }
        saveLastPlayed();
    }

    @Override
    protected void onDestroy() {
        stopSeekbarUpdates();
        super.onDestroy();
    }

    private void saveLastPlayed() {
        if (listSongs == null || listSongs.isEmpty() || position < 0 || position >= listSongs.size()) {
            return;
        }
        MusicFiles current = listSongs.get(position);
        SharedPreferences.Editor editor = getSharedPreferences(MusicService.MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MusicService.MUSIC_FILE, current.getPath());
        editor.putString(MusicService.ARTIST_NAME, current.getArtist());
        editor.putString(MusicService.SONG_NAME, current.getTitle());
        editor.putString(NowPlayingFragmentBottom.ARTWORK_URL, current.getArtworkUrl());
        editor.apply();
    }

    private void updateNowPlayingStateFromCurrent() {
        saveLastPlayed();
        NowPlayingFragmentBottom.setLayoutVisible();
        NowPlayingFragmentBottom.refreshFromState();
    }

    public void prevBtnClicked() {
        if (musicService == null || listSongs == null || listSongs.isEmpty()) {
            return;
        }
        MusicFiles previousSong = currentSong();
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if (previousSong != null) {
                clearSavedPositionForSong(previousSong);
            }
            if (shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album_name.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
            passMusicService = musicService;
            updateNowPlayingStateFromCurrent();
        }
        else {
            musicService.stop();
            musicService.release();
            if (previousSong != null) {
                clearSavedPositionForSong(previousSong);
            }
            if (shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album_name.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            passMusicService = musicService;
            updateNowPlayingStateFromCurrent();
        }
    }

    public void nextBtnClicked() {
        if (musicService == null || listSongs == null || listSongs.isEmpty()) {
            return;
        }
        MusicFiles previousSong = currentSong();
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if (previousSong != null) {
                clearSavedPositionForSong(previousSong);
            }
            if (shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            //else position will be position
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album_name.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
            passMusicService = musicService;
            updateNowPlayingStateFromCurrent();
        }
        else {
            musicService.stop();
            musicService.release();
            if (previousSong != null) {
                clearSavedPositionForSong(previousSong);
            }
            if (shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            //else position will be position
//            position = ((position + 1) % listSongs.size());
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album_name.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            passMusicService = musicService;
            updateNowPlayingStateFromCurrent();
        }
    }

    public void playPauseBtnClicked() {
        if (musicService == null) {
            return;
        }
        if (musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            if (shuffleBoolean && !repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            passMusicService = musicService;
        }
        else{
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            if (shuffleBoolean && !repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            }else if (!shuffleBoolean && repeatBoolean){
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }else if (shuffleBoolean && repeatBoolean){
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
            seekBar.setMax(musicService.getDuration() / 1000);
            syncSeekBarWithPlayer();
            startSeekbarUpdates();
            passMusicService = musicService;
        }
    }

//    public void PassingBottomFrag(){
//        uri = Uri.parse(listSongs.get(position).getPath());
//        metaData(uri);
//        byte[] artPhoto = artist_image;
//        if (artPhoto != null){
//            Glide.with(getBaseContext()).load(artPhoto)
//                    .into(NowPlayingFragmentBottom.albumArt);
//        }else{
////                Toast.makeText(musicService, "Art is NULL!!!", Toast.LENGTH_SHORT).show();
//        }
//        NowPlayingFragmentBottom.songName.setText(listSongs.get(position).getTitle());
//        NowPlayingFragmentBottom.artist.setText(listSongs.get(position).getArtist());
//    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1){
            return totalNew;
        }
        else{
            return totalout;
        }
    }

    private void getIntenMethod() {
        // Album tab da dung cung flow voi Home, khong con sender=albumDetails.
        position = getIntent().getIntExtra("positionMfiles", -1);
        listSongs = mFiles;

        if (listSongs == null || listSongs.isEmpty() || position < 0 || position >= listSongs.size()) {
            Toast.makeText(this, "Song not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MusicFiles selectedSong = listSongs.get(position);
        uri = Uri.parse(selectedSong.getPath());

        // Luon uu tien attach neu bai duoc bam chinh la bai dang phat.
        boolean isSameCurrentSong = passMusicService != null && isCurrentServiceSong(selectedSong);
        boolean requestedAttachOnly = getIntent().getBooleanExtra(EXTRA_ATTACH_ONLY, false);
        isAttachOnlyMode = isSameCurrentSong || requestedAttachOnly;

        if (isAttachOnlyMode) {
            boolean isPlaying = false;
            try {
                isPlaying = passMusicService != null && passMusicService.isPlaying();
            } catch (Exception ignored) {
            }
            playPauseBtn.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            return;
        }

        playPauseBtn.setImageResource(R.drawable.ic_pause);
        if (musicService != null){
            musicService.stop();
            musicService.release();
        }

        int resumeMs = resolveResumePositionMs(selectedSong);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        intent.putExtra(MusicService.EXTRA_START_POSITION_MS, resumeMs);
        startService(intent);
    }

    private boolean isCurrentServiceSong(MusicFiles selectedSong) {
        if (selectedSong == null) {
            return false;
        }
        SharedPreferences pref = getSharedPreferences(MusicService.MUSIC_LAST_PLAYED, MODE_PRIVATE);
        String currentPath = pref.getString(MusicService.MUSIC_FILE, null);
        String currentTitle = pref.getString(MusicService.SONG_NAME, null);
        String currentArtist = pref.getString(MusicService.ARTIST_NAME, null);

        String selectedPath = selectedSong.getPath();
        boolean samePath = isSameSongPath(selectedPath, currentPath);
        boolean sameMeta = isSameSongMeta(selectedSong, currentTitle, currentArtist);
        return samePath || sameMeta;
    }

    private int resolveResumePositionMs(MusicFiles song) {
        String key = buildSongPositionPreferenceKey(song);
        if (key.isEmpty()) {
            return -1;
        }
        int saved = getSharedPreferences(MusicService.MUSIC_LAST_PLAYED, MODE_PRIVATE).getInt(key, -1);
        if (saved <= 0) {
            return -1;
        }
        int durationSeconds = safeDurationSeconds(song != null ? song.getDuration() : null);
        int durationMs = durationSeconds > 0 ? durationSeconds * 1000 : -1;
        if (durationMs > 0 && saved >= durationMs) {
            return Math.max(0, durationMs - 1000);
        }
        return saved;
    }

    private boolean isSameSongPath(String leftPath, String rightPath) {
        if (leftPath == null || rightPath == null) {
            return false;
        }
        return leftPath.trim().equalsIgnoreCase(rightPath.trim());
    }

    private boolean isSameSongMeta(MusicFiles song, String title, String artist) {
        if (song == null || title == null || artist == null) {
            return false;
        }
        String songTitle = song.getTitle();
        String songArtist = song.getArtist();
        if (songTitle == null || songArtist == null) {
            return false;
        }
        return songTitle.trim().equalsIgnoreCase(title.trim())
                && songArtist.trim().equalsIgnoreCase(artist.trim());
    }

    private int safeDurationSeconds(String durationRaw) {
        if (durationRaw == null) {
            return 0;
        }
        try {
            long parsed = Long.parseLong(durationRaw.trim());
            if (parsed <= 0) {
                return 0;
            }
            return parsed > 1000 ? (int) (parsed / 1000L) : (int) parsed;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String buildSongPositionPreferenceKey(MusicFiles song) {
        if (song == null) {
            return "";
        }
        String path = song.getPath();
        if (path != null && !path.trim().isEmpty()) {
            return SONG_POSITION_PREFIX + path.trim();
        }
        String title = song.getTitle() != null ? song.getTitle().trim() : "";
        String artist = song.getArtist() != null ? song.getArtist().trim() : "";
        if (title.isEmpty() && artist.isEmpty()) {
            return "";
        }
        return SONG_POSITION_PREFIX + title + "_" + artist;
    }

    private void metaData(Uri songUri) {
        if (songUri == null) {
            return;
        }

        int durationSeconds = 0;
        if (musicService != null) {
            durationSeconds = musicService.getDuration() / 1000;
        }
        if (durationSeconds <= 0 && listSongs != null && position >= 0 && position < listSongs.size()) {
            durationSeconds = safeDurationSeconds(listSongs.get(position).getDuration());
        }
        duration_total.setText(formattedTime(Math.max(durationSeconds, 0)));

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = null;
        try {
            retriever.setDataSource(this, songUri);
            art = retriever.getEmbeddedPicture();
        } catch (Exception ignored) {
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }

        artist_image = art;
        if (art != null) {
            Glide.with(this).load(art).into(cover_art);
            Glide.with(this).load(art).into(blurBackground);
            applyBlurIfSupported(blurBackground);
            return;
        }

        String artworkUrl = null;
        if (listSongs != null && position >= 0 && position < listSongs.size()) {
            artworkUrl = listSongs.get(position).getArtworkUrl();
        }
        if (artworkUrl != null && !artworkUrl.trim().isEmpty()) {
            Glide.with(this).load(artworkUrl).into(cover_art);
            Glide.with(this).load(artworkUrl).into(blurBackground);
            applyBlurIfSupported(blurBackground);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)  {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        if (listSongs == null || listSongs.isEmpty() || position < 0 || position >= listSongs.size()) {
            return;
        }
        int durationFromPlayer = musicService.getDuration();
        int durationSeconds = durationFromPlayer > 0
                ? durationFromPlayer / 1000
                : safeDurationSeconds(listSongs.get(position).getDuration());
        seekBar.setMax(durationSeconds);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        album_name.setText(listSongs.get(position).getAlbum());
        musicService.OnCompleted();

        int notifAndUiIcon;
        if (isAttachOnlyMode) {
            notifAndUiIcon = musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play;
        } else {
            notifAndUiIcon = R.drawable.ic_pause;
        }
        musicService.showNotification(notifAndUiIcon);
        playPauseBtn.setImageResource(notifAndUiIcon);

        passMusicService = musicService;
        syncSeekBarWithPlayer();
        startSeekbarUpdates();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    private void initViews(){
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        album_name = findViewById(R.id.song_album);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        blurBackground = findViewById(R.id.blur_background);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        favoriteBtn = findViewById(R.id.playerFavoriteBtn);
        addPlaylistBtn = findViewById(R.id.playerAddPlaylistBtn);
    }

    private MusicFiles currentSong() {
        if (listSongs == null || listSongs.isEmpty() || position < 0 || position >= listSongs.size()) {
            return null;
        }
        return listSongs.get(position);
    }

    private void onFavoriteClicked() {
        FirebaseUser user = com.example.konomusic.auth.AuthManager.currentUser();
        MusicFiles song = currentSong();
        if (user == null) {
            pendingAuthAction = PENDING_FAVORITE;
            com.example.konomusic.auth.AuthManager.openLoginForResult(this, com.example.konomusic.auth.AuthManager.LOGIN_REQUEST_CODE);
            return;
        }
        if (song == null) {
            return;
        }

        libraryRepository.toggleFavorite(user.getUid(), song, new com.example.konomusic.data.repository.UserLibraryRepository.FavoriteStateCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                isCurrentFavorite = isFavorite;
                updateFavoriteIcon();
                Toast.makeText(PlayerActivity.this,
                        getString(isFavorite ? R.string.player_favorite_added : R.string.player_favorite_removed),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAddPlaylistClicked() {
        FirebaseUser user = com.example.konomusic.auth.AuthManager.currentUser();
        MusicFiles song = currentSong();
        if (user == null) {
            pendingAuthAction = PENDING_ADD_TO_PLAYLIST;
            com.example.konomusic.auth.AuthManager.openLoginForResult(this, com.example.konomusic.auth.AuthManager.LOGIN_REQUEST_CODE);
            return;
        }
        if (song == null) {
            return;
        }

        libraryRepository.loadPlaylists(user.getUid(), new com.example.konomusic.data.repository.UserLibraryRepository.PlaylistsCallback() {
            @Override
            public void onResult(ArrayList<com.example.konomusic.data.repository.UserLibraryRepository.PlaylistItem> playlists) {
                showPlaylistDialog(user.getUid(), song, playlists);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistDialog(String uid, MusicFiles song, ArrayList<com.example.konomusic.data.repository.UserLibraryRepository.PlaylistItem> playlists) {
        PlaylistDialogUi.showPicker(
                this,
                playlists,
                () -> showCreatePlaylistDialog(uid, song),
                (selected, onCountUpdated) -> libraryRepository.addSongToPlaylist(uid, selected.getPlaylistId(), song, new com.example.konomusic.data.repository.UserLibraryRepository.ResultCallback() {
                    @Override
                    public void onSuccess() {
                        if (onCountUpdated != null) {
                            onCountUpdated.run();
                        }
                        Toast.makeText(PlayerActivity.this, R.string.player_playlist_added, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void showCreatePlaylistDialog(String uid, MusicFiles song) {
        PlaylistDialogUi.showNameInput(
                this,
                getString(R.string.player_playlist_new_title),
                getString(R.string.playlist_name_dialog_subtitle),
                "",
                name -> libraryRepository.createPlaylistAndAddSong(uid, name, song, new com.example.konomusic.data.repository.UserLibraryRepository.ResultCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(PlayerActivity.this, R.string.player_playlist_created_and_added, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void refreshFavoriteState() {
        FirebaseUser user = com.example.konomusic.auth.AuthManager.currentUser();
        MusicFiles song = currentSong();
        if (user == null || song == null) {
            isCurrentFavorite = false;
            updateFavoriteIcon();
            return;
        }

        libraryRepository.isFavorite(user.getUid(), song, new com.example.konomusic.data.repository.UserLibraryRepository.FavoriteStateCallback() {
            @Override
            public void onResult(boolean isFavorite) {
                isCurrentFavorite = isFavorite;
                updateFavoriteIcon();
            }

            @Override
            public void onError(String message) {
                isCurrentFavorite = false;
                updateFavoriteIcon();
            }
        });
    }

    private void updateFavoriteIcon() {
        if (favoriteBtn == null) {
            return;
        }
        favoriteBtn.setImageResource(isCurrentFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
    }

    private void updateModeButtonsUi() {
        if (shuffleBtn == null || repeatBtn == null) {
            return;
        }

        shuffleBtn.setImageResource(shuffleBoolean ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_off);
        repeatBtn.setImageResource(repeatBoolean ? R.drawable.ic_repeat_on : R.drawable.ic_repeat_off);

        int active = ContextCompat.getColor(this, R.color.spotify_green);
        int inactive = ContextCompat.getColor(this, R.color.text_secondary);
        shuffleBtn.setColorFilter(shuffleBoolean ? active : inactive);
        repeatBtn.setColorFilter(repeatBoolean ? active : inactive);
    }

    private void applyBlurIfSupported(ImageView target) {
        if (target == null) {
            return;
        }
        target.setAlpha(0.25f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            target.setRenderEffect(RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP));
        }
    }

    public boolean isColorDark(int color){
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private void syncSeekBarWithPlayer() {
        if (musicService == null) {
            return;
        }

        int durationSecondsFromPlayer = musicService.getDuration() / 1000;
        int durationSecondsFallback = 0;
        if (listSongs != null && position >= 0 && position < listSongs.size()) {
            durationSecondsFallback = safeDurationSeconds(listSongs.get(position).getDuration());
        }

        int durationSeconds = Math.max(durationSecondsFromPlayer, durationSecondsFallback);
        if (durationSeconds > 0 && seekBar.getMax() != durationSeconds) {
            seekBar.setMax(durationSeconds);
        }

        int currentSeconds = Math.max(0, musicService.getCurrentPosition() / 1000);
        if (seekBar.getMax() > 0 && currentSeconds > seekBar.getMax()) {
            currentSeconds = seekBar.getMax();
        }

        seekBar.setProgress(currentSeconds);
        duration_played.setText(formattedTime(currentSeconds));
    }

    private void startSeekbarUpdates() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    private void stopSeekbarUpdates() {
        handler.removeCallbacks(progressRunnable);
    }

    private void clearSavedPositionForSong(MusicFiles song) {
        String key = buildSongPositionPreferenceKey(song);
        if (key.isEmpty()) {
            return;
        }
        getSharedPreferences(MusicService.MUSIC_LAST_PLAYED, MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply();
    }
}
