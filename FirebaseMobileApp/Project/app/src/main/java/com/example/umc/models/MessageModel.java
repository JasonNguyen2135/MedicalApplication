package com.example.umc.models;

import com.google.firebase.Timestamp;

public class MessageModel {
    private String senderId;
    private String content;
    private Timestamp createdAt;

    public MessageModel() {}

    public MessageModel(String senderId, String content, Timestamp createdAt) {
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
}
