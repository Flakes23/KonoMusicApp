package com.example.konomusic.model;

public class StatCard {
    private int iconResId;
    private String label;
    private String count;
    private int accentColor;
    private String trend;
    private boolean trendUp;

    public StatCard(int iconResId, String label, String count, int accentColor, String trend, boolean trendUp) {
        this.iconResId = iconResId;
        this.label = label;
        this.count = count;
        this.accentColor = accentColor;
        this.trend = trend;
        this.trendUp = trendUp;
    }

    public int getIconResId() { return iconResId; }
    public String getLabel() { return label; }
    public String getCount() { return count; }
    public int getAccentColor() { return accentColor; }
    public String getTrend() { return trend; }
    public boolean isTrendUp() { return trendUp; }
}

