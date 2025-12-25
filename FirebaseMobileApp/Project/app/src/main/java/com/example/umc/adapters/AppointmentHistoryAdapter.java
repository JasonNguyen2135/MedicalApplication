package com.example.umc.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.Appointment;
import com.example.umc.ui.AppointmentDetailActivity;

import java.util.List;

public class AppointmentHistoryAdapter
        extends RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> list;

    public AppointmentHistoryAdapter(Context context, List<Appointment> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Appointment a = list.get(position);

        // ===== TIME =====
        String[] dt = parseDateTime(a.getAppointmentTime());
        h.tvDate.setText("Ngày: " + dt[0]);
        h.tvTime.setText("Giờ: " + dt[1]);

        // ===== DOCTOR =====
        h.tvDoctor.setText("Bác sĩ: " + safe(a.getDoctorName()));

        // ===== STATUS =====
        h.tvStatus.setText("Trạng thái: " + safe(convertStatus(a.getStatus())));

        // ===== PRICE =====
        if (a.getPrice() != null) {
            h.tvPrice.setText("Giá: " + a.getPrice().intValue() + " VNĐ");
        } else {
            h.tvPrice.setText("Giá: —");
        }

        // ===== PAYMENT =====
        if (a.isPaid()) {
            h.tvPaymentStatus.setText("Đã thanh toán");
            h.tvPaymentStatus.setTextColor(
                    context.getColor(android.R.color.holo_green_dark));
        } else {
            h.tvPaymentStatus.setText("Chưa thanh toán");
            h.tvPaymentStatus.setTextColor(
                    context.getColor(android.R.color.holo_red_dark));
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, AppointmentDetailActivity.class);
            i.putExtra("appointmentId", a.getId());
            context.startActivity(i);
        });
    }
    public String convertStatus(String status) {
        switch (status) {
            case "PENDING":
                return "Đang chờ";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "COMPLETED":
                return "Đã hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "CANCELLED_BY_USER":
                return "Bạn đã hủy ";
            case "CANCELLED_BY_DOCTOR":
                return "Đã hủy bởi ";
            case "CANCELLED_BY_SYSTEM":
                return "Đã bị hủy";
            case "APPROVED":
                return "Đã duyệt";
            case "BOOKED":
                return "Đã đặt";
            case "HOLDING":
                return "Đang giữ chỗ";
            default:
                return "Không xác định";
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDoctor, tvDate, tvTime, tvStatus, tvPaymentStatus, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDoctor = itemView.findViewById(R.id.tvAppointmentDoctor);
            tvDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }

    // ================= HELPERS =================
    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "—" : s;
    }

    private String[] parseDateTime(String raw) {
        if (raw == null) return new String[]{"—", "—"};

        raw = raw.replace("T", " ");
        String[] parts = raw.split(" ");

        String date = parts.length > 0 ? parts[0] : "—";
        String time = parts.length > 1
                ? parts[1].substring(0, Math.min(5, parts[1].length()))
                : "—";

        return new String[]{date, time};
    }
}
