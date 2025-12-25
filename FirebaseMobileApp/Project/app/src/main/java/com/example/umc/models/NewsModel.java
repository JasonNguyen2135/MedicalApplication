package com.example.umc.models;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

public class NewsModel {

    @DocumentId
    private String id;   // 🔥 ID của document

    private String title;
    private String content;
    private String createdBy;
    private Date createdAt;

    public NewsModel() {}

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCreatedBy() { return createdBy; }
    public Date getCreatedAt() { return createdAt; }
}
