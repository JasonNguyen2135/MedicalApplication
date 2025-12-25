package com.example.umc.models;


public class HomeItem {

    public String emoji;
    public String title;
    public Class<?> targetActivity;

    public HomeItem(String emoji, String title, Class<?> targetActivity) {
        this.emoji = emoji;
        this.title = title;
        this.targetActivity = targetActivity;
    }
}
