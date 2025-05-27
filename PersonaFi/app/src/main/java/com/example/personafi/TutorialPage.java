package com.example.personafi;

public class TutorialPage {
    private int imageResId;
    private String title;
    private String description;

    public TutorialPage(int imageResId, String title, String description) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
} 