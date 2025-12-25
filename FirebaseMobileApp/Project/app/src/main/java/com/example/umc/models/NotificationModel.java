package com.example.umc.models;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String id;
    private String userId;
    private String title;
    private String content;
    private String type;        // CHAT | APPOINTMENT | PAY | SYSTEM
    private boolean read;
    private Timestamp createdAt;
    private String targetId;    // appointmentId | chatId | newsId

    // 🔥 BẮT BUỘC CHO FIRESTORE
    public NotificationModel() {}

    // 🔥 CONSTRUCTOR DÙNG KHI ADD
    public NotificationModel(
            String userId,
            String type,
            String title,
            String content,
            String targetId
    ) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetId = targetId;
        this.read = false;
        this.createdAt = Timestamp.now();
    }

    // ===== GET =====
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getTargetId() { return targetId; }

    // ===== SET =====
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setType(String type) { this.type = type; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
