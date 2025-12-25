package com.example.umc.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.ChatAIAdapter;
import com.example.umc.models.ChatMessage;
import com.example.umc.ai.GeminiApi;

import java.util.ArrayList;

public class ChatAIFragment extends Fragment {

    RecyclerView rvMessages;
    EditText etMessage;
    Button btnSend;

    ChatAIAdapter adapter;
    ArrayList<ChatMessage> messages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat_ai, container, false);

        rvMessages = v.findViewById(R.id.rvMessages);
        etMessage = v.findViewById(R.id.etMessage);
        btnSend = v.findViewById(R.id.btnSend);

        adapter = new ChatAIAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);
        Log.d("CHAT_UI", "rv=" + rvMessages + ", et=" + etMessage + ", btn=" + btnSend);
        btnSend.setOnClickListener(view -> sendMessage());

        return v;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // Add user message
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        etMessage.setText("");

        callGemini(text);
    }

    private void callGemini(String userMessage) {

        GeminiApi.sendMessage(userMessage, new GeminiApi.GeminiCallback() {

            @Override
            public void onSuccess(String reply) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    messages.add(new ChatMessage(reply, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    messages.add(new ChatMessage("Lỗi: " + error, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                });
            }
        });
    }
}
