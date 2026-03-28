package com.example.konomusic.shared.utils;

/**
 * Constants - Hằng số chung cho ứng dụng
 */
public class Constants {

    // API Config
    public static final String API_BASE_URL = "http://192.168.1.100:8080/api";
    public static final int CONNECT_TIMEOUT = 15000;
    public static final int READ_TIMEOUT = 15000;

    // Shared Preferences
    public static final String PREF_NAME = "KonoMusicAppPref";
    public static final String PREF_TOKEN = "auth_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_EMAIL = "user_email";

    // Database
    public static final String DB_NAME = "konomusic.db";

    // Pagination
    public static final int PAGE_SIZE = 20;

}

