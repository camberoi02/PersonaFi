package com.example.personafi;

public class Achievement {
    private String name;
    private String description;
    private boolean unlocked;
    private int iconResourceId;

    public Achievement(String name, String description, int iconResourceId) {
        this.name = name;
        this.description = description;
        this.unlocked = false;
        this.iconResourceId = iconResourceId;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }
} 