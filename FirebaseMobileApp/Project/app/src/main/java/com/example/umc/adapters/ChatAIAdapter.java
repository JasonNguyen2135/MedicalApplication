package com.example.umc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.ChatMessage;

import java.util.ArrayList;
import io.noties.markwon.Markwon;


public class ChatAIAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ChatMessage> messages;

    public ChatAIAdapter(ArrayList<ChatMessage> list) {
        this.messages = list;
    }

    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        if (viewType == TYPE_USER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_bot, parent, false);
            return new BotVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        if (holder instanceof UserVH) {
            ((UserVH) holder).txt.setText(msg.getText());
        } else {
            BotVH bot = (BotVH) holder;

            Markwon markwon = Markwon.create(bot.txt.getContext());
            markwon.setMarkdown(bot.txt, msg.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView txt;

        UserVH(View v) {
            super(v);
            txt = v.findViewById(R.id.txtUserMessage);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView txt;

        BotVH(View v) {
            super(v);
            txt = v.findViewById(R.id.txtBotMessage);
        }
    }
}
