package com.example.konomusic.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.konomusic.R;
import com.example.konomusic.core.app.MainActivity;
import com.example.konomusic.domain.model.MusicFiles;
import com.example.konomusic.playback.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.konomusic.playback.MusicService.passPosition;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    public static final String ARTWORK_URL = "ARTWORK_URL";

    private static NowPlayingFragmentBottom mInstance;
    static RelativeLayout bottom_bac_frag;
    static ImageView albumArt;
    static TextView artist, songName;
    public static FloatingActionButton playPauseBtn;

    private ImageView nextBtn, prevBtn;
    private View rootView;
    private MusicService musicService;
    private boolean bindservice = false;

    private String lastRenderedPath;
    private String lastRenderedArtworkUrl;

    public NowPlayingFragmentBottom() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        mInstance = this;

        bottom_bac_frag = rootView.findViewById(R.id.card_bottom_player);
        albumArt = rootView.findViewById(R.id.bottom_album_art);
        artist = rootView.findViewById(R.id.song_artist_miniPlayer);
        songName = rootView.findViewById(R.id.song_name_miniPlayer);
        nextBtn = rootView.findViewById(R.id.skip_next_bottom);
        prevBtn = rootView.findViewById(R.id.skip_prev_bottom);
        playPauseBtn = rootView.findViewById(R.id.play_pause_miniPlayer);

        rootView.setVisibility(View.GONE);

        nextBtn.setOnClickListener(v -> {
            if (musicService != null) {
                // Next/Prev trong app hien tai luon chuyen bai va auto-play.
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                musicService.nextBtnClicked();
                renderFromState();
                rootView.postDelayed(this::syncPlayPauseIcon, 120);
                rootView.postDelayed(this::syncPlayPauseIcon, 420);
            }
        });

        prevBtn.setOnClickListener(v -> {
            if (musicService != null) {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                musicService.prevBtnClicked();
                renderFromState();
                rootView.postDelayed(this::syncPlayPauseIcon, 120);
                rootView.postDelayed(this::syncPlayPauseIcon, 420);
            }
        });

        playPauseBtn.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.playPauseBtnClicked();
                rootView.postDelayed(this::syncPlayPauseIcon, 120);
            }
        });

        rootView.setOnClickListener(v -> {
            if (getContext() == null) {
                return;
            }
            int position = passPosition < 0 ? 0 : passPosition;
            int startPositionMs = getCurrentPlaybackPositionMs();
            Intent playerIntent = new Intent(getContext(), PlayerActivity.class);
            playerIntent.putExtra("musicAdapter", "MusicAdapt");
            playerIntent.putExtra("positionMfiles", position);
            playerIntent.putExtra(MusicService.EXTRA_START_POSITION_MS, startPositionMs);
            startActivity(playerIntent);
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderFromState();
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), MusicService.class);
            getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
            bindservice = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null && bindservice) {
            getContext().unbindService(this);
            bindservice = false;
        }
    }

    private void renderFromState() {
        if (getActivity() == null) {
            return;
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE, null);
        String artistName = preferences.getString(ARTIST_NAME, null);
        String songNameValue = preferences.getString(SONG_NAME, null);
        String artworkUrl = preferences.getString(ARTWORK_URL, null);

        boolean visible = path != null && !path.isEmpty();
        if (!visible) {
            lastRenderedPath = null;
            lastRenderedArtworkUrl = null;
            setLayoutInvisible();
            return;
        }

        setLayoutVisible();

        if (songName != null) {
            songName.setText(songNameValue == null ? "" : songNameValue);
        }
        if (artist != null) {
            artist.setText(artistName == null ? "" : artistName);
        }

        if (albumArt == null) {
            return;
        }

        String resolvedArtworkUrl = resolveArtworkUrl(path, songNameValue, artistName, artworkUrl);
        boolean isStream = isStreamPath(path);

        boolean samePath = path.equals(lastRenderedPath);
        boolean sameArtwork = (resolvedArtworkUrl == null && lastRenderedArtworkUrl == null)
                || (resolvedArtworkUrl != null && resolvedArtworkUrl.equals(lastRenderedArtworkUrl));
        if (samePath && sameArtwork && albumArt.getDrawable() != null) {
            return;
        }

        if (isStream) {
            if (resolvedArtworkUrl != null && !resolvedArtworkUrl.trim().isEmpty()) {
                Glide.with(requireContext())
                        .load(resolvedArtworkUrl)
                        .centerCrop()
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .dontAnimate()
                        .into(albumArt);
                lastRenderedArtworkUrl = resolvedArtworkUrl;
            } else {
                albumArt.setImageResource(R.drawable.musicicon);
                lastRenderedArtworkUrl = null;
            }
        } else {
            byte[] art = getAlbumArt(path);
            if (art != null) {
                Glide.with(requireContext())
                        .asBitmap()
                        .load(art)
                        .centerCrop()
                        .placeholder(R.drawable.musicicon)
                        .error(R.drawable.musicicon)
                        .into(albumArt);
            } else {
                albumArt.setImageResource(R.drawable.musicicon);
            }
            lastRenderedArtworkUrl = null;
        }

        lastRenderedPath = path;
    }

    private boolean isStreamPath(String path) {
        if (path == null) {
            return false;
        }
        String lower = path.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private String resolveArtworkUrl(String path, String title, String artistName, String prefArtworkUrl) {
        if (prefArtworkUrl != null && !prefArtworkUrl.trim().isEmpty()) {
            return prefArtworkUrl;
        }

        String fromMain = findArtworkInList(MainActivity.musicFiles, path, title, artistName);
        if (fromMain != null) {
            return fromMain;
        }

        String fromAdapter = findArtworkInList(MusicAdapter.mFiles, path, title, artistName);
        if (fromAdapter != null) {
            return fromAdapter;
        }

        return findArtworkInList(PlayerActivity.listSongs, path, title, artistName);
    }

    private String findArtworkInList(ArrayList<MusicFiles> list, String path, String title, String artistName) {
        if (list == null) {
            return null;
        }

        String normPath = normalizePath(path);
        for (MusicFiles file : list) {
            if (file == null) {
                continue;
            }

            String url = file.getArtworkUrl();
            if (url == null || url.trim().isEmpty()) {
                continue;
            }

            String normItemPath = normalizePath(file.getPath());
            if (normPath != null && normPath.equals(normItemPath)) {
                return url;
            }

            String itemTitle = file.getTitle() == null ? "" : file.getTitle();
            String itemArtist = file.getArtist() == null ? "" : file.getArtist();
            if (title != null && artistName != null
                    && title.equalsIgnoreCase(itemTitle)
                    && artistName.equalsIgnoreCase(itemArtist)) {
                return url;
            }
        }

        return null;
    }

    private String normalizePath(String value) {
        if (value == null) {
            return null;
        }
        String decoded = Uri.decode(value).trim();
        return decoded.isEmpty() ? null : decoded;
    }

    private byte[] getAlbumArt(String path) {
        if (path == null || isStreamPath(path)) {
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
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

    private void syncPlayPauseIcon() {
        if (playPauseBtn == null) {
            return;
        }

        MusicService service = musicService != null ? musicService : PlayerActivity.passMusicService;
        if (service != null && service.isPlayingOrWillPlay()) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_play);
        }
    }

    public static void refreshFromState() {
        if (mInstance != null) {
            mInstance.renderFromState();
            mInstance.syncPlayPauseIcon();
        }
    }

    public static void setLayoutVisible() {
        if (mInstance != null && mInstance.rootView != null) {
            mInstance.rootView.setVisibility(View.VISIBLE);
        }

        MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            View container = activity.findViewById(R.id.frag_bottom_player);
            if (container != null) {
                container.setVisibility(View.VISIBLE);
            }
        }

        if (bottom_bac_frag != null) {
            bottom_bac_frag.setVisibility(View.VISIBLE);
        }
    }

    public static void setLayoutInvisible() {
        if (mInstance != null && mInstance.rootView != null) {
            mInstance.rootView.setVisibility(View.GONE);
        }

        MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            View container = activity.findViewById(R.id.frag_bottom_player);
            if (container != null) {
                container.setVisibility(View.GONE);
            }
        }

        if (bottom_bac_frag != null) {
            bottom_bac_frag.setVisibility(View.GONE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        syncPlayPauseIcon();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    private int getCurrentPlaybackPositionMs() {
        if (musicService != null) {
            try {
                return musicService.getCurrentPosition();
            } catch (Exception ignored) {
            }
        }

        if (PlayerActivity.passMusicService != null) {
            try {
                return PlayerActivity.passMusicService.getCurrentPosition();
            } catch (Exception ignored) {
            }
        }
        return -1;
    }
}
