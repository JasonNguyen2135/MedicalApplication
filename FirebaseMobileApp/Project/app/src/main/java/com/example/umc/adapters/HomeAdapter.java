package com.example.umc.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.HomeItem;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private final Context context;
    private final List<HomeItem> items = new ArrayList<>();
    private final List<HomeItem> fullList = new ArrayList<>();

    public HomeAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<HomeItem> data) {
        items.clear();
        items.addAll(data);

        fullList.clear();
        fullList.addAll(data);

        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        items.clear();

        if (keyword.isEmpty()) {
            items.addAll(fullList);
        } else {
            keyword = keyword.toLowerCase();
            for (HomeItem i : fullList) {
                if (i.title.toLowerCase().contains(keyword)) {
                    items.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_home_grid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        HomeItem item = items.get(position);

        h.icon.setText(item.emoji);
        h.title.setText(item.title);

        h.itemView.setOnClickListener(v ->
                context.startActivity(new Intent(context, item.targetActivity))
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.tvIcon);
            title = v.findViewById(R.id.tvTitle);
        }
    }
}
