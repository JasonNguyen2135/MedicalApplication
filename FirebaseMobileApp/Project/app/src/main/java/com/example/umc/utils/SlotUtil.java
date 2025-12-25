package com.example.umc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SlotUtil {

    // start = "08:00", end = "17:00"
    public static List<String> generateSlots(
            String start, String end, int slotMinutes
    ) {
        List<String> slots = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(start));

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(sdf.parse(end));

            while (cal.before(endCal)) {
                slots.add(sdf.format(cal.getTime()));
                cal.add(Calendar.MINUTE, slotMinutes);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return slots;
    }
}
