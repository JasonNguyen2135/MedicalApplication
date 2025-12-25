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
import com.example.umc.utils.ChatUtil;
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

public class ChatActivity extends AppCompatActivity {

    RecyclerView rv;
    EditText et;
    Button btn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    String adminId = "SuHKStIIR1Xo3JJBMaDQ1DsWMAc2"; // 🔥 thay bằng uid admin
    String chatId;

    List<MessageModel> list = new ArrayList<>();
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_chat);

        rv = findViewById(R.id.rvChat);
        et = findViewById(R.id.etMessage);
        btn = findViewById(R.id.btnSend);

        rv.setLayoutManager(new LinearLayoutManager(this));
        chatId = ChatUtil.buildChatId(uid, adminId);

        adapter = new ChatAdapter(list, uid);
        rv.setAdapter(adapter);
        MaterialToolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

// bật nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

// xử lý click
        toolbar.setNavigationOnClickListener(v -> finish());

        String userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            toolbar.setTitle(userName);
        }


        loadMessages();

        btn.setOnClickListener(v -> send());
    }

    private void loadMessages() {
        if (chatId == null) {
            Log.w("ChatActivity", "loadMessages: chatId is null");
            return;
        }

        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.w("ChatActivity", "loadMessages:onError", e);
                        return;
                    }
                    if (snap == null) return;

                    list.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
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
                            Log.w("ChatActivity", "skip malformed message doc: " + d.getId());
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
            Log.w("ChatActivity", "send: chatId is null, aborting");
            return;
        }

        String msg = et.getText().toString().trim();
        if (msg.isEmpty()) return;

        MessageModel local = new MessageModel(uid, msg, Timestamp.now());

        // Optimistically update UI
        list.add(local);
        adapter.notifyItemInserted(list.size() - 1);
        rv.scrollToPosition(list.size() - 1);
        et.setText("");

        // Persist message with server timestamp
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", uid);
        data.put("content", msg);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .add(data)
                .addOnFailureListener(err -> {
                    Log.w("ChatActivity", "send: failed to add message", err);
                });

        // Update/create chat meta
        Map<String, Object> chat = new HashMap<>();
        chat.put("userId", uid);
        chat.put("adminId", adminId);
        chat.put("userName", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        chat.put("lastMessage", msg);
        chat.put("updatedAt", FieldValue.serverTimestamp());
        NotificationUtil.notifyAllAdmins(
                db,
                "Tin nhắn mới",
                "Người dùng vừa gửi cho bạn một tin nhắn",
                "CHAT",
                chatId // 🔥
        );


        db.collection("chats").document(chatId).set(chat, SetOptions.merge());
    }

}
