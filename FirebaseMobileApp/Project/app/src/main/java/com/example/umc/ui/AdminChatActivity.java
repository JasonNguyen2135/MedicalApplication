package com.example.umc.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.ChatAdapter;
import com.example.umc.models.MessageModel;
import com.example.umc.utils.NotificationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminChatActivity extends AppCompatActivity {

    RecyclerView rv;
    EditText et;
    Button btn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    String adminId = "l4i06fRK4IhDjvBqey52RdfdkKo1"; // 🔥 thay bằng uid admin
    String chatId;

    List<MessageModel> list = new ArrayList<>();
    ChatAdapter adapter;
    String userId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_chat);

        rv = findViewById(R.id.rvChat);
        et = findViewById(R.id.etMessage);
        btn = findViewById(R.id.btnSend);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(list, FirebaseAuth.getInstance().getUid());
        rv.setAdapter(adapter);

        chatId = getIntent().getStringExtra("chatId");
        String userName = getIntent().getStringExtra("userName");
        MaterialToolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

// bật nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userId = getIntent().getStringExtra("userId");
// xử lý click
        toolbar.setNavigationOnClickListener(v -> finish());

        setTitle("Chat với " + userName);

        loadMessages();

        btn.setOnClickListener(v -> send());
    }

    private void loadMessages() {
        if (chatId == null) {
            Log.w("AdminChatActivity", "loadMessages: chatId is null");
            return;
        }

        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.w("AdminChatActivity", "loadMessages:onError", e);
                        return;
                    }
                    if (snap == null) return;

                    list.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        // read fields with fallbacks to avoid mapping issues
                        String sender = null;
                        String content = null;
                        Timestamp createdAt = null;

                        if (d.contains("senderId")) sender = d.getString("senderId");
                        else if (d.contains("sender")) sender = d.getString("sender");

                        if (d.contains("content")) content = d.getString("content");
                        else if (d.contains("text")) content = d.getString("text");
                        else if (d.contains("message")) content = d.getString("message");

                        createdAt = d.getTimestamp("createdAt");
                        if (createdAt == null) createdAt = d.getTimestamp("created_at");

                        if (sender == null || content == null) {
                            // skip malformed doc but log
                            Log.w("AdminChatActivity", "skip malformed message doc: " + d.getId());
                            continue;
                        }

                        MessageModel m = new MessageModel(sender, content, createdAt != null ? createdAt : Timestamp.now());
                        list.add(m);
                    }

                    adapter.notifyDataSetChanged();

                    if (!list.isEmpty()) {
                        rv.scrollToPosition(list.size() - 1);
                    }

                });
    }


    private void send() {

        if (chatId == null) {
            Log.w("AdminChatActivity", "send: chatId is null, aborting");
            return;
        }

        String msg = et.getText().toString().trim();
        if (msg.isEmpty()) return;

        // Optimistic local message
        MessageModel local = new MessageModel(uid, msg, Timestamp.now());
        list.add(local);
        adapter.notifyItemInserted(list.size() - 1);
        rv.scrollToPosition(list.size() - 1);
        et.setText("");

        // Persist message using server timestamp to ensure ordering and persistence
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", uid);
        data.put("content", msg);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .add(data)
                .addOnFailureListener(err -> {
                    Log.w("AdminChatActivity", "send: failed to add message", err);
                    // Optionally mark message as failed in UI
                });

        // update/create chat document (merge to avoid failure if doc missing)
        Map<String, Object> chat = new HashMap<>();
        chat.put("lastMessage", msg);
        chat.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    String realUserId = doc.getString("userId");

                    NotificationUtil.create(
                            db,
                            realUserId,
                            "Admin đã trả lời",
                            "Bạn có tin nhắn mới từ admin",
                            "CHAT",
                            chatId
                    );
                });


        db.collection("chats").document(chatId).set(chat, SetOptions.merge());
    }

}
