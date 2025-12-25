package com.example.umc.models;

import com.google.firebase.Timestamp;

public class ChatModel {
    private String userId;
    private String userName;
    private String adminId;
    private String lastMessage;
    private Timestamp updatedAt;
    private String chatId; // <--- new field

    public ChatModel() {}

    // ...existing getters...
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // new getter for chatId
    public String getChatId() {
        return chatId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // new setter for chatId
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
