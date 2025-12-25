package com.example.umc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.Appointment;
import com.example.umc.utils.NotificationUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminAppointmentAdapter
        extends RecyclerView.Adapter<AdminAppointmentAdapter.VH> {

    public interface Click {
        void open(Appointment a);
    }

    List<Appointment> list;
    Click click;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminAppointmentAdapter(List<Appointment> list, Click c) {
        this.list = list;
        this.click = c;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_appointment, p, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        Appointment a = list.get(i);

        h.time.setText(a.getAppointmentTime());
        h.status.setText(a.getStatus());

        // click mở chi tiết
        h.itemView.setOnClickListener(v -> click.open(a));

        // click xóa
        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                confirmDelete(v, a, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ===== CONFIRM DELETE =====
    private void confirmDelete(View v, Appointment a, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("Xóa lịch khám")
                .setMessage("Bạn chắc chắn muốn xóa lịch khám này?")
                .setPositiveButton("Xóa", (d, w) -> deleteAppointment(a, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ===== DELETE =====
    private void deleteAppointment(Appointment a, int position) {

        db.collection("appointments")
                .document(a.getId())
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    Appointment ap = doc.toObject(Appointment.class);
                    if (ap == null) return;

                    boolean isPaid = Boolean.TRUE.equals(ap.isPaid());
                    String userId = ap.getUserId();

                    // 🔥 1. XÓA LỊCH
                    db.collection("appointments")
                            .document(a.getId())
                            .delete()
                            .addOnSuccessListener(s -> {

                                // update UI admin
                                if (position >= 0 && position < list.size()) {
                                    list.remove(position);
                                    notifyItemRemoved(position);
                                }

                                // 🔔 2. GỬI THÔNG BÁO CHO USER
                                if (userId != null) {

                                    if (isPaid) {
                                        // 💰 ĐÃ THANH TOÁN → HOÀN TIỀN
                                        NotificationUtil.create(
                                                db,
                                                userId,
                                                "Lịch khám bị hủy",
                                                "Lịch khám đã bị admin hủy. Tiền sẽ được hoàn lại.",
                                                "REFUND",
                                                a.getId()
                                        );
                                    } else {
                                        // ❌ CHƯA THANH TOÁN
                                        NotificationUtil.create(
                                                db,
                                                userId,
                                                "Lịch khám bị hủy",
                                                "Lịch khám của bạn đã bị admin hủy.",
                                                "CANCEL",
                                                a.getId()
                                        );
                                    }
                                }
                            });
                });
    }


    static class VH extends RecyclerView.ViewHolder {
        TextView time, status;
        ImageButton btnDelete;

        VH(View v) {
            super(v);
            time = v.findViewById(R.id.tvTime);
            status = v.findViewById(R.id.tvStatus);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
