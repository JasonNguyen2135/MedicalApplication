package com.example.umc.utils;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {

    // ================= CREATE ONE =================
    public static void create(
            FirebaseFirestore db,
            String userId,
            String title,
            String content,
            String type,
            String targetId // 🔥 appointmentId | chatId | newsId
    ) {
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("title", title);
        data.put("content", content);
        data.put("type", type);
        data.put("targetId", targetId);
        data.put("read", false);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("notifications").add(data);
    }

    // ================= NOTIFY ALL ADMINS =================
    public static void notifyAllAdmins(
            FirebaseFirestore db,
            String title,
            String content,
            String type,
            String targetId
    ) {
        db.collection("users")
                .whereEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(qs -> {
                    for (var d : qs) {
                        create(
                                db,
                                d.getId(), // ✅ admin uid
                                title,
                                content,
                                type,
                                targetId
                        );
                    }
                });
    }
}
