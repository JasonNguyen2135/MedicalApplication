package com.example.umc.models;

import java.util.List;

public class WorkSchedule {
    public String startTime;   // "08:00"
    public String endTime;     // "17:00"
    public List<Integer> workingDays; // 2-6 (T2–T6)
    public List<String> dayOffs; // "2025-12-20"

    public WorkSchedule() {}
}
