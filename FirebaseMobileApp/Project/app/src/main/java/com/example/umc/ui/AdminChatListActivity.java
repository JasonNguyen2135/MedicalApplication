package com.example.umc.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.AdminChatAdapter;
import com.example.umc.models.ChatModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminChatListActivity extends AppCompatActivity {

    RecyclerView rv;
    List<ChatModel> list = new ArrayList<>();
    AdminChatAdapter adapter;

    // keep per-chat listener registrations to remove later
    Map<String, ListenerRegistration> latestMsgListeners = new HashMap<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_chat_list);

        rv = findViewById(R.id.rvChats);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminChatAdapter(list, this::confirmDeleteChat);
        rv.setAdapter(adapter);

        rv.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// bật nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

// xử lý click
        toolbar.setNavigationOnClickListener(v -> finish());

        db.collection("chats")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.w("AdminChatListActivity", "onError", e);
                        return;
                    }
                    if (snap == null) return;

                    // remove existing per-chat listeners to avoid duplicates/leaks
                    for (ListenerRegistration reg : latestMsgListeners.values()) {
                        try { reg.remove(); } catch (Exception ignored) {}
                    }
                    latestMsgListeners.clear();

                    list.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        // try mapping first, fallback to manual read if needed
                        ChatModel cm = d.toObject(ChatModel.class);
                        if (cm == null || cm.getUserId() == null) {
                            cm = new ChatModel();
                            cm.setUserId(d.getString("userId"));
                            cm.setUserName(d.getString("userName"));
                            cm.setAdminId(d.getString("adminId"));
                            cm.setLastMessage(d.getString("lastMessage"));
                            cm.setUpdatedAt(d.getTimestamp("updatedAt"));
                        }
                        // skip entries missing userId
                        if (cm.getUserId() == null) {
                            Log.w("AdminChatListActivity", "skip chat doc without userId: " + d.getId());
                            continue;
                        }

                        // set chatId on model so listeners can reliably locate it later
                        cm.setChatId(d.getId());

                        // add immediately so the user appears in the list
                        list.add(cm);
                        final String chatId = d.getId();

                        // attach a real-time listener to the latest message for this chat
                        ListenerRegistration reg = db.collection("messages")
                                .document(chatId)
                                .collection("messages")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener((msnap, merr) -> {
                                    if (merr != null) {
                                        Log.w("AdminChatListActivity", "latestMsg onError for " + chatId, merr);
                                        return;
                                    }
                                    if (msnap == null) return;
                                    if (msnap.isEmpty()) return;

                                    DocumentSnapshot md = msnap.getDocuments().get(0);
                                    String last = null;
                                    if (md.contains("content")) last = md.getString("content");
                                    else if (md.contains("message")) last = md.getString("message");
                                    else if (md.contains("text")) last = md.getString("text");

                                    Timestamp ts = md.getTimestamp("createdAt");
                                    if (last != null || ts != null) {
                                        // create final copies so lambdas can capture them
                                        final String lastCopy = last;
                                        final Timestamp tsCopy = ts;

                                        runOnUiThread(() -> {
                                            // find the model by chatId (no captured mutable index)
                                            int idx = -1;
                                            for (int j = 0; j < list.size(); j++) {
                                                ChatModel cm2 = list.get(j);
                                                if (chatId.equals(cm2.getChatId())) {
                                                    idx = j;
                                                    break;
                                                }
                                            }
                                            if (idx == -1) return;

                                            ChatModel existing = list.get(idx);
                                            // update only when non-null
                                            if (lastCopy != null) {
                                                existing.setLastMessage(lastCopy);
                                            }
                                            if (tsCopy != null) {
                                                existing.setUpdatedAt(tsCopy);
                                            }

                                            sortListByUpdatedAtDesc();
                                            adapter.notifyDataSetChanged();
                                        });
                                    }
                                });

                        latestMsgListeners.put(chatId, reg);
                    }

                    // initial sort and notify
                    sortListByUpdatedAtDesc();
                    adapter.notifyDataSetChanged();
                });
    }
    private void confirmDeleteChat(ChatModel chat) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xoá đoạn chat?")
                .setMessage("Toàn bộ tin nhắn sẽ bị xoá vĩnh viễn.")
                .setPositiveButton("Xoá", (d, w) -> deleteChat(chat))
                .setNegativeButton("Huỷ", null)
                .show();
    }
    private void deleteChat(ChatModel chat) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatId = chat.getChatId();

        // remove listener nếu đang nghe
        ListenerRegistration reg = latestMsgListeners.remove(chatId);
        if (reg != null) reg.remove();

        // 1️⃣ Xoá messages
        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        d.getReference().delete();
                    }

                    // 2️⃣ Xoá chat document
                    db.collection("chats")
                            .document(chatId)
                            .delete()
                            .addOnSuccessListener(a -> {
                                list.remove(chat);
                                adapter.notifyDataSetChanged();
                            });
                });
    }


    private void sortListByUpdatedAtDesc() {
        Collections.sort(list, new Comparator<ChatModel>() {
            @Override
            public int compare(ChatModel a, ChatModel b) {
                Timestamp ta = a.getUpdatedAt();
                Timestamp tb = b.getUpdatedAt();
                if (ta == null && tb == null) return 0;
                if (ta == null) return 1;
                if (tb == null) return -1;
                // newer first
                return tb.compareTo(ta);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cleanup listeners
        for (ListenerRegistration reg : latestMsgListeners.values()) {
            try { reg.remove(); } catch (Exception ignored) {}
        }
        latestMsgListeners.clear();
    }
}
