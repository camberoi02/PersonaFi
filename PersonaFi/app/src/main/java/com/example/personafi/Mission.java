package com.example.personafi;

public class Mission {
    private String title;
    private String description;
    private int xpReward;
    private int currentProgress;
    private int targetProgress;
    private boolean isCompleted;
    private boolean isDaily;
    private long lastUpdated;

    public Mission(String title, String description, int xpReward, int targetProgress, boolean isDaily) {
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.targetProgress = targetProgress;
        this.currentProgress = 0;
        this.isCompleted = false;
        this.isDaily = isDaily;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getXpReward() { return xpReward; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTargetProgress() { return targetProgress; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isDaily() { return isDaily; }
    public long getLastUpdated() { return lastUpdated; }

    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
        if (this.currentProgress >= this.targetProgress) {
            this.isCompleted = true;
        }
    }

    public void setLastUpdated(long timestamp) {
        this.lastUpdated = timestamp;
    }

    public int getProgressPercentage() {
        return (currentProgress * 100) / targetProgress;
    }
} 