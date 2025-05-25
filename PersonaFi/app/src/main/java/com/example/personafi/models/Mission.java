package com.example.personafi.models;

public class Mission {
    private String title;
    private int points;
    private boolean completed;

    public Mission(String title, int points) {
        this.title = title;
        this.points = points;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
} 