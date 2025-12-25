package com.example.umc.utils;

import com.example.umc.models.Doctor;
import com.example.umc.models.WorkSchedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DoctorScheduleUtil {

    // dateStr: "yyyy-MM-dd"
    public static boolean isDoctorWorking(Doctor d, String dateStr) {

        if (d == null || d.getWorkSchedule() == null) return false;

        WorkSchedule s = d.getWorkSchedule();

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1–7

            // ❌ Không nằm trong ngày làm việc
            if (s.workingDays == null ||
                    !s.workingDays.contains(dayOfWeek)) {
                return false;
            }

            // ❌ Nghỉ phép
            if (s.dayOffs != null &&
                    s.dayOffs.contains(dateStr)) {
                return false;
            }

            return true;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isTimeValid(Doctor doctor, String appointmentTime) {
        if (doctor == null || doctor.getWorkSchedule() == null) return false;
        if (appointmentTime == null) return false;

        try {
            String[] dt = appointmentTime.split(" ");
            String date = dt[0];
            String time = dt[1];

            // kiểm tra ngày làm việc
            if (!isDoctorWorking(doctor, date)) return false;

            // kiểm tra giờ
            String start = doctor.getWorkSchedule().startTime;
            String end = doctor.getWorkSchedule().endTime;

            return time.compareTo(start) >= 0 && time.compareTo(end) < 0;

        } catch (Exception e) {
            return false;
        }
    }

}
