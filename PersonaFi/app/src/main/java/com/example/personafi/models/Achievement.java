package com.example.personafi.models;

public class Achievement {
    private int iconResId;
    private String title;
    private boolean unlocked;

    public Achievement(int iconResId, String title, boolean unlocked) {
        this.iconResId = iconResId;
        this.title = title;
        this.unlocked = unlocked;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
} 