package com.example.umc.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton; // Đổi từ Button sang ImageButton

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.ChatAIAdapter;
import com.example.umc.ai.AiPromptBuilder;
import com.example.umc.ai.GeminiApi;
import com.example.umc.models.ChatMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // sử dụng FloatingActionButton cho btnSend
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatAIActivity extends AppCompatActivity {

    RecyclerView rvMessages;
    EditText etMessage;
    FloatingActionButton btnSend; // Sửa kiểu từ MaterialButton -> FloatingActionButton
    ImageButton btnBack;    // Nút quay lại mới

    ChatAIAdapter adapter;
    ArrayList<ChatMessage> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        // 1. Ánh xạ đúng ID từ XML mới
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack); // Ánh xạ nút quay lại

        // 2. Xử lý nút quay lại thay cho Toolbar cũ
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Thiết lập RecyclerView
        adapter = new ChatAIAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // Tùy chọn: Đẩy tin nhắn từ dưới lên
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        // 4. Xử lý gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // Thêm tin nhắn của User
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        etMessage.setText("");

        // Gọi AI
        loadDoctorsAndAskAI(text);
    }
    private void loadDoctorsAndAskAI(String userMessage) {

        FirebaseFirestore.getInstance()
                .collection("doctors")
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder doctorInfo = new StringBuilder();

                    for (DocumentSnapshot d : snapshot) {
                        String name = d.getString("name");
                        String speciality = d.getString("speciality");
                        Long exp = d.getLong("experience");

                        doctorInfo.append("- ")
                                .append(name)
                                .append(" | ")
                                .append(speciality)
                                .append(" | ")
                                .append(exp != null ? exp : 0)
                                .append(" năm kinh nghiệm\n");
                    }

                    String prompt = AiPromptBuilder.build(userMessage, doctorInfo.toString());

                    GeminiApi.sendMessage(prompt, new GeminiApi.GeminiCallback() {
                        @Override
                        public void onSuccess(String reply) {
                            runOnUiThread(() -> {
                                messages.add(new ChatMessage(reply, false));
                                adapter.notifyItemInserted(messages.size() - 1);
                                rvMessages.scrollToPosition(messages.size() - 1);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                messages.add(new ChatMessage("AI lỗi: " + error, false));
                                adapter.notifyItemInserted(messages.size() - 1);
                            });
                        }
                    });

                });
    }


    private void callGemini(String userMessage) {
        // Có thể thêm một tin nhắn "Đang suy nghĩ..." ở đây để trải nghiệm tốt hơn
        GeminiApi.sendMessage(userMessage, new GeminiApi.GeminiCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    messages.add(new ChatMessage(reply, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    messages.add(new ChatMessage("Lỗi kết nối AI: " + error, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                });
            }
        });
    }
}