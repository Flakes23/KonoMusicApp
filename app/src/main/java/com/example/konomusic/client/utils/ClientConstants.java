package com.example.konomusic.client.utils;

/**
 * ClientConstants - Hằng số cho CLIENT
 */
public class ClientConstants {

    // Tags for logging
    public static final String TAG = "KonoMusicClient";

    // SharedPreferences
    public static final String PREF_NAME = "KonoMusic_Client_Prefs";
    public static final String KEY_TOKEN = "auth_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";

    // Intent keys
    public static final String INTENT_VIDEO_ID = "video_id";
    public static final String INTENT_VIDEO = "video";
    public static final String INTENT_PLAYLIST_ID = "playlist_id";

    // Request codes
    public static final int REQUEST_LOGIN = 100;
    public static final int REQUEST_REGISTER = 101;

    // Debounce delay for search (milliseconds)
    public static final long DEBOUNCE_DELAY_MS = 500;

    // API
    public static final String BASE_URL = "http://192.168.1.100:8080/api/";
    public static final int TIMEOUT_SECONDS = 30;
    public static final int PAGE_SIZE = 10;

}

