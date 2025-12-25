package com.example.umc.utils;

import com.example.umc.models.Doctor;

import java.sql.Timestamp;

public class NoShowUtil {

    // appointmentDate: yyyy-MM-dd
    public static boolean isNoShow(
            String appointmentDate,
            Doctor doctor
    ) {
        if (doctor == null || doctor.getWorkSchedule() == null) return false;

        try {
            String endTime = doctor.getWorkSchedule().endTime; // "17:00"

            // ghép thành: 2025-12-20 17:00:00
            String endDateTime =
                    appointmentDate + " " + endTime + ":00";

            long endMillis =
                    Timestamp.valueOf(endDateTime).getTime();

            // +2 tiếng
            long noShowMillis = endMillis + 2 * 60 * 60 * 1000;

            return System.currentTimeMillis() > noShowMillis;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
