package com.example.konomusic.model;

public class RecentActivity {
    public static final int TYPE_UPLOAD = 0;
    public static final int TYPE_DELETE = 1;
    public static final int TYPE_EDIT   = 2;
    public static final int TYPE_USER   = 3;

    private String title;
    private String subtitle;
    private String timestamp;
    private int type;

    public RecentActivity(String title, String subtitle, String timestamp, int type) {
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getTimestamp() { return timestamp; }
    public int getType() { return type; }
}

