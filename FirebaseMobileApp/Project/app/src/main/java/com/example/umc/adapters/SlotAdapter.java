package com.example.umc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;

import java.util.List;
import java.util.Set;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.VH> {

    public interface OnSlotClick {
        void onClick(String slot);
    }


    List<String> slots;
    Set<String> bookedSlots;
    OnSlotClick listener;

    public SlotAdapter(List<String> slots,
                       Set<String> bookedSlots,
                       OnSlotClick listener) {
        this.slots = slots;
        this.bookedSlots = bookedSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_slot, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        String slot = slots.get(i);
        h.btn.setText(slot);

        boolean locked = bookedSlots.contains(slot);
        h.btn.setEnabled(!locked);
        h.btn.setAlpha(locked ? 0.4f : 1f);

        h.btn.setOnClickListener(v -> listener.onClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView btn;
        VH(View v) {
            super(v);
            btn = v.findViewById(R.id.btnSlot);
        }
    }
}
