package com.example.konomusic.playback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.renderscript.Script;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.konomusic.R;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.ui.player.NowPlayingFragmentBottom;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.konomusic.core.app.ApplicationClass.ACTION_NEXT;
import static com.example.konomusic.core.app.ApplicationClass.ACTION_PLAY;
import static com.example.konomusic.core.app.ApplicationClass.ACTION_PREVIOUS;
import static com.example.konomusic.core.app.ApplicationClass.CHANNEL_ID_2;
import static com.example.konomusic.ui.player.PlayerActivity.listSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    public static final String ARTWORK_URL = "ARTWORK_URL";
    public static final String EXTRA_START_POSITION_MS = "startPositionMs";
    private static final String SONG_POSITION_PREFIX = "SONG_POS_";
    public static int passPosition;
    boolean isPreparing = false;
    boolean pendingPlay = false;
    private int playbackSessionToken = 0;

    NotificationManager notificationManager;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String lastCountedSongKey = null;
    private String pendingNotificationArtworkKey = null;
    private int pendingSeekPositionMs = -1;

    private final Handler playbackStateHandler = new Handler(Looper.getMainLooper());
    private final Runnable playbackStateTicker = new Runnable() {
        @Override
        public void run() {
            updatePlaybackState();
            if (mediaPlayer != null && isPlaying()) {
                playbackStateHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),  "My Audio");
        try {
            musicFiles = listSongs;

        } catch (NullPointerException e) {
            System.err.println("Null pointer exception");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }

    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.w("MusicService", "onStartCommand called with null intent");
            return START_STICKY;
        }
        int myPosition = intent.getIntExtra("servicePosition", -1);
        int startPositionMs = intent.getIntExtra(EXTRA_START_POSITION_MS, -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1){
            try {
                playMedia(myPosition, startPositionMs);

            } catch (NullPointerException e) {
                System.err.println("Null pointer exception");
            }
        }
        if (actionName != null){
            switch (actionName){
                case "playPause":
//                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    playPauseBtnClicked();
                    break;
                case "next":
//                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    nextBtnClicked();
                    break;
                case "previous":
//                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    prevBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition, int startPositionMs) {
        musicFiles = listSongs;
        if (musicFiles == null || musicFiles.isEmpty()) {
            return;
        }

        int safeStart = startPosition;
        if (safeStart < 0 || safeStart >= musicFiles.size()) {
            safeStart = 0;
        }

        MusicFiles previousSong = getSongAt(position);
        MusicFiles targetSong = getSongAt(safeStart);
        boolean switchedSong = previousSong != null && targetSong != null && !isSameSong(previousSong, targetSong);

        pendingPlay = true;
        playbackSessionToken++;
        pendingSeekPositionMs = startPositionMs;

        // Khi đổi sang bài khác, xóa progress đã lưu của bài cũ theo yêu cầu.
        if (switchedSong) {
            clearSavedPositionForSong(previousSong);
        }

        releasePlayer(false);
        position = safeStart;

        createMediaPlayer(position);
        if (mediaPlayer != null && !isPreparing) {
            applyPendingSeekIfNeeded(mediaPlayer);
            mediaPlayer.start();
            pendingPlay = false;
            trackPlayCountIfNeeded();
            startPlaybackStateSync();
        }
        updatePlaybackState();
        NowPlayingFragmentBottom.refreshFromState();
    }

    public void start() {
        pendingPlay = true;
        if (mediaPlayer != null && !isPreparing) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    trackPlayCountIfNeeded();
                }
                pendingPlay = false;
                startPlaybackStateSync();
                updatePlaybackState();
            } catch (IllegalStateException e) {
                Log.e("MusicService", "start() in invalid state", e);
                pendingPlay = false;
            }
        }
    }

    public boolean isPlaying(){
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isPlayingOrWillPlay() {
        return pendingPlay || isPreparing || isPlaying();
    }

    public void stop(){
        saveCurrentPositionForCurrentSong();
        pendingPlay = false;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                Log.e("MusicService", "stop() in invalid state", e);
            }
        }
        stopPlaybackStateSync();
        updatePlaybackState();
    }

    public void release(){
        saveCurrentPositionForCurrentSong();
        pendingPlay = false;
        releasePlayer();
        stopPlaybackStateSync();
        updatePlaybackState();
    }

    public void persistPositionNow() {
        saveCurrentPositionForCurrentSong();
    }

    public int getDuration(){
        if (mediaPlayer == null || isPreparing) {
            return 0;
        }
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position){
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            updatePlaybackState();
        }
    }

    public int getCurrentPosition(){
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public void createMediaPlayer(int positionInner){
        position = positionInner;
        final int sessionToken = playbackSessionToken;
        Uri parsed = null;
        try {
            parsed = Uri.parse(musicFiles.get(position).getPath());
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception in createmediaplayer");
        }
        uri = parsed;
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri != null ? uri.toString() : "");
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.putString(ARTWORK_URL, musicFiles.get(position).getArtworkUrl());
        editor.apply();

        if (uri == null) {
            mediaPlayer = null;
            isPreparing = false;
            updatePlaybackState();
            return;
        }

        String uriString = uri.toString();
        isPreparing = false;
        if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
            MediaPlayer player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            try {
                player.setDataSource(uriString);
                isPreparing = true;
                player.setOnPreparedListener(mp -> {
                    // Ignore stale callbacks from old players released by a newer request.
                    if (sessionToken != playbackSessionToken || mp != mediaPlayer) {
                        try {
                            mp.reset();
                            mp.release();
                        } catch (Exception ignored) {
                        }
                        return;
                    }
                    isPreparing = false;
                    applyPendingSeekIfNeeded(mp);
                    if (pendingPlay) {
                        mp.start();
                        pendingPlay = false;
                        trackPlayCountIfNeeded();
                    }
                    if (mp.isPlaying()) {
                        startPlaybackStateSync();
                    }
                    updatePlaybackState();
                });
                player.setOnErrorListener((mp, what, extra) -> {
                    if (sessionToken != playbackSessionToken || mp != mediaPlayer) {
                        try {
                            mp.reset();
                            mp.release();
                        } catch (Exception ignored) {
                        }
                        return true;
                    }
                    isPreparing = false;
                    pendingPlay = false;
                    Log.e("MusicService", "MediaPlayer error what=" + what + ", extra=" + extra);
                    releasePlayer();
                    updatePlaybackState();
                    return true;
                });
                player.prepareAsync();
                mediaPlayer = player;
                updatePlaybackState();
                return;
            } catch (IOException e) {
                Log.e("MusicService", "Failed to setDataSource for stream: " + uriString, e);
                isPreparing = false;
                mediaPlayer = null;
                pendingPlay = false;
                updatePlaybackState();
                return;
            }
        }

        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
        isPreparing = false;
        if (mediaPlayer == null) {
            Log.e("MusicService", "Failed to create MediaPlayer for uri: " + uri);
            pendingPlay = false;
            updatePlaybackState();
            return;
        }
        applyPendingSeekIfNeeded(mediaPlayer);
        updatePlaybackState();
    }

    private void releasePlayer() {
        releasePlayer(true);
    }

    private void releasePlayer(boolean saveCurrentPosition) {
        if (mediaPlayer != null) {
            if (saveCurrentPosition) {
                saveCurrentPositionForCurrentSong();
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
        isPreparing = false;
        pendingSeekPositionMs = -1;
        stopPlaybackStateSync();
        updatePlaybackState();
    }

    public void pause(){
        saveCurrentPositionForCurrentSong();
        pendingPlay = false;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (IllegalStateException e) {
                Log.e("MusicService", "pause() in invalid state", e);
            }
        }
        stopPlaybackStateSync();
        updatePlaybackState();
    }

    public void OnCompleted(){
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        clearSavedPositionForCurrentSong();
        // Chi xu ly 1 luong next de tranh tao 2 MediaPlayer cung luc gay be tieng.
        if (actionPlaying != null){
            actionPlaying.nextBtnClicked();
            return;
        }
        skipInternal(1);
    }

    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn){
        if (!refreshQueue()) {
            return;
        }
        if (position < 0 || position >= musicFiles.size()) {
            return;
        }

        MusicFiles current = musicFiles.get(position);
        String title = current.getTitle();
        String artist = current.getArtist();

        byte[] picture = getAlbumArt(current.getPath());
        Bitmap thumb;
        if (picture != null){
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else{
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.musicicon);
        }

        postNowPlayingNotification(playPauseBtn, thumb, title, artist);
        requestArtworkForNotificationIfNeeded(current, playPauseBtn, title, artist);
        NowPlayingFragmentBottom.refreshFromState();
    }

    private void postNowPlayingNotification(int playPauseBtn, Bitmap largeIcon, String title, String artist) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, getPendingIntentFlags());

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, getPendingIntentFlags());

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, getPendingIntentFlags());

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, getPendingIntentFlags());

        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, safeCurrentDurationMs())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, largeIcon)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, largeIcon)
                .build());
        mediaSessionCompat.setActive(true);

        passPosition = position;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(artist)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setContentIntent(contentIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(2, notification);
        notificationManager =
                (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(2, notification);
        updatePlaybackState();
    }

    private void requestArtworkForNotificationIfNeeded(MusicFiles current, int playPauseBtn, String title, String artist) {
        String artworkUrl = current.getArtworkUrl();
        if (artworkUrl == null || artworkUrl.trim().isEmpty()) {
            return;
        }

        String currentPath = current.getPath();
        if (currentPath == null) {
            return;
        }
        String lower = currentPath.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return;
        }

        final String songKey = buildSongNotificationKey(current);
        pendingNotificationArtworkKey = songKey;
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(artworkUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        if (songKey.equals(buildCurrentNotificationSongKey()) && songKey.equals(pendingNotificationArtworkKey)) {
                            postNowPlayingNotification(playPauseBtn, resource, title, artist);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        // Keep fallback icon if artwork load fails.
                    }
                });
    }

    private String buildSongNotificationKey(MusicFiles song) {
        if (song == null) {
            return "";
        }
        String path = song.getPath() == null ? "" : song.getPath();
        String title = song.getTitle() == null ? "" : song.getTitle();
        String artist = song.getArtist() == null ? "" : song.getArtist();
        return path + "|" + title + "|" + artist;
    }

    private String buildCurrentNotificationSongKey() {
        if (musicFiles == null || position < 0 || position >= musicFiles.size()) {
            return "";
        }
        return buildSongNotificationKey(musicFiles.get(position));
    }

    private byte[] getAlbumArt(String sourcePath){
        if (sourcePath == null) {
            return null;
        }
        String lower = sourcePath.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(sourcePath);
            return retriever.getEmbeddedPicture();
        } catch (RuntimeException e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private int getPendingIntentFlags() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    private boolean refreshQueue() {
        try {
            musicFiles = listSongs;
        } catch (Exception ignored) {
        }
        return musicFiles != null && !musicFiles.isEmpty();
    }

    private void playCurrentInternal() {
        if (!refreshQueue()) {
            return;
        }
        if (position < 0 || position >= musicFiles.size()) {
            position = 0;
        }
        playMedia(position, -1);
        OnCompleted();
        if (mediaPlayer != null) {
            showNotification(R.drawable.ic_pause);
        }
    }

    private void trackPlayCountIfNeeded() {
        if (musicFiles == null || musicFiles.isEmpty() || position < 0 || position >= musicFiles.size()) {
            return;
        }

        MusicFiles current = musicFiles.get(position);
        if (current == null) {
            return;
        }

        String songKey = buildSongKey(current);
        if (songKey == null || songKey.isEmpty()) {
            return;
        }

        if (songKey.equals(lastCountedSongKey)) {
            return;
        }
        lastCountedSongKey = songKey;

        String songId = current.getId();
        if (songId != null && !songId.trim().isEmpty() && current.isFromFirebase()) {
            firestore.collection("songs")
                    .document(songId)
                    .update(
                            "playCount", FieldValue.increment(1),
                            "updatedAt", FieldValue.serverTimestamp()
                    )
                    .addOnFailureListener(e -> Log.w("MusicService", "playCount update failed by id", e));
            return;
        }

        String streamUrl = current.getStreamUrl();
        if (streamUrl == null || streamUrl.trim().isEmpty()) {
            String path = current.getPath();
            if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
                streamUrl = path;
            }
        }

        if (streamUrl == null || streamUrl.trim().isEmpty()) {
            return;
        }

        firestore.collection("songs")
                .whereEqualTo("streamUrl", streamUrl)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        query.getDocuments().get(0).getReference().update(
                                "playCount", FieldValue.increment(1),
                                "updatedAt", FieldValue.serverTimestamp()
                        ).addOnFailureListener(e -> Log.w("MusicService", "playCount update failed by streamUrl", e));
                    }
                })
                .addOnFailureListener(e -> Log.w("MusicService", "playCount lookup failed", e));
    }

    private String buildSongKey(MusicFiles song) {
        if (song.getId() != null && !song.getId().trim().isEmpty() && song.isFromFirebase()) {
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

    private void resetPlayCountGuardForCurrentSong() {
        lastCountedSongKey = null;
    }

    private void skipInternal(int delta) {
        if (!refreshQueue()) {
            return;
        }

        if (musicFiles.size() == 1) {
            // Chi 1 bai: khong doi bai, chi phat lai tu dau de tranh crash.
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                if (!mediaPlayer.isPlaying() && !isPreparing) {
                    resetPlayCountGuardForCurrentSong();
                    mediaPlayer.start();
                    trackPlayCountIfNeeded();
                }
                showNotification(R.drawable.ic_pause);
            } else {
                playCurrentInternal();
            }
            return;
        }

        int next = (position + delta) % musicFiles.size();
        if (next < 0) {
            next += musicFiles.size();
        }
        resetPlayCountGuardForCurrentSong();
        playMedia(next, -1);
        OnCompleted();
        if (mediaPlayer != null) {
            showNotification(R.drawable.ic_pause);
        }
    }

    private void applyPendingSeekIfNeeded(MediaPlayer player) {
        if (player == null || pendingSeekPositionMs <= 0) {
            return;
        }
        try {
            int duration = player.getDuration();
            int safe = pendingSeekPositionMs;
            if (duration > 0) {
                safe = Math.min(pendingSeekPositionMs, Math.max(0, duration - 1000));
            }
            if (safe > 0) {
                player.seekTo(safe);
            }
        } catch (Exception ignored) {
        } finally {
            pendingSeekPositionMs = -1;
            updatePlaybackState();
        }
    }

    private void saveCurrentPositionForCurrentSong() {
        if (musicFiles == null || position < 0 || position >= musicFiles.size() || mediaPlayer == null) {
            return;
        }
        MusicFiles current = musicFiles.get(position);
        String key = buildSongPositionPreferenceKey(current);
        if (key.isEmpty()) {
            return;
        }
        int currentMs;
        try {
            currentMs = mediaPlayer.getCurrentPosition();
        } catch (Exception ignored) {
            return;
        }
        getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                .edit()
                .putInt(key, Math.max(currentMs, 0))
                .apply();
    }

    private void clearSavedPositionForCurrentSong() {
        if (musicFiles == null || position < 0 || position >= musicFiles.size()) {
            return;
        }
        clearSavedPositionForSong(musicFiles.get(position));
    }

    private void clearSavedPositionForSong(MusicFiles song) {
        String key = buildSongPositionPreferenceKey(song);
        if (key.isEmpty()) {
            return;
        }
        getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply();
    }

    private String buildSongPositionPreferenceKey(MusicFiles song) {
        String songKey = buildSongKey(song);
        if (songKey == null || songKey.trim().isEmpty()) {
            return "";
        }
        return SONG_POSITION_PREFIX + songKey.trim();
    }

    public void playPauseBtnClicked(){
        if (actionPlaying != null){
            actionPlaying.playPauseBtnClicked();
            return;
        }

        // Fallback cho mini-player khi PlayerActivity khong dang callback.
        if (!refreshQueue()) {
            return;
        }
        if (mediaPlayer == null) {
            playCurrentInternal();
            return;
        }

        if (isPlaying()) {
            pause();
            showNotification(R.drawable.ic_play);
        } else {
            start();
            showNotification(R.drawable.ic_pause);
        }
    }

    public void nextBtnClicked(){
        if (actionPlaying != null){
            actionPlaying.nextBtnClicked();
            return;
        }
        skipInternal(1);
    }

    public void prevBtnClicked(){
        if (actionPlaying != null){
            actionPlaying.prevBtnClicked();
            return;
        }
        skipInternal(-1);
    }

    private MusicFiles getSongAt(int index) {
        if (musicFiles == null || index < 0 || index >= musicFiles.size()) {
            return null;
        }
        return musicFiles.get(index);
    }

    private boolean isSameSong(MusicFiles left, MusicFiles right) {
        String leftKey = buildSongKey(left);
        String rightKey = buildSongKey(right);
        if (leftKey != null && !leftKey.trim().isEmpty() && rightKey != null && !rightKey.trim().isEmpty()) {
            return leftKey.equals(rightKey);
        }

        String leftPath = left != null ? left.getPath() : null;
        String rightPath = right != null ? right.getPath() : null;
        if (leftPath != null && rightPath != null && leftPath.equals(rightPath)) {
            return true;
        }

        String leftTitle = left != null ? left.getTitle() : null;
        String rightTitle = right != null ? right.getTitle() : null;
        String leftArtist = left != null ? left.getArtist() : null;
        String rightArtist = right != null ? right.getArtist() : null;
        return leftTitle != null && leftArtist != null
                && leftTitle.equals(rightTitle)
                && leftArtist.equals(rightArtist);
    }

    private void startPlaybackStateSync() {
        playbackStateHandler.removeCallbacks(playbackStateTicker);
        playbackStateHandler.post(playbackStateTicker);
    }

    private void stopPlaybackStateSync() {
        playbackStateHandler.removeCallbacks(playbackStateTicker);
    }

    private void updatePlaybackState() {
        if (mediaSessionCompat == null) {
            return;
        }

        int state;
        long positionMs = 0L;
        float speed = 0f;

        if (mediaPlayer == null) {
            state = PlaybackStateCompat.STATE_STOPPED;
        } else {
            try {
                positionMs = Math.max(0, mediaPlayer.getCurrentPosition());
            } catch (Exception ignored) {
            }

            if (isPreparing) {
                state = PlaybackStateCompat.STATE_BUFFERING;
                speed = 0f;
            } else if (mediaPlayer.isPlaying()) {
                state = PlaybackStateCompat.STATE_PLAYING;
                speed = 1f;
            } else {
                state = PlaybackStateCompat.STATE_PAUSED;
                speed = 0f;
            }
        }

        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SEEK_TO;

        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, positionMs, speed, SystemClock.elapsedRealtime())
                .build());
    }

    private long safeCurrentDurationMs() {
        if (mediaPlayer != null && !isPreparing) {
            try {
                return Math.max(0, mediaPlayer.getDuration());
            } catch (Exception ignored) {
            }
        }

        if (musicFiles != null && position >= 0 && position < musicFiles.size()) {
            try {
                return Math.max(0, Long.parseLong(musicFiles.get(position).getDuration()));
            } catch (Exception ignored) {
            }
        }
        return 0L;
    }
}
