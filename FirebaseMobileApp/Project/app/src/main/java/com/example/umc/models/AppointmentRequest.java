package com.example.umc.models;

public class AppointmentRequest {
    public String doctorId;
    public String appointmentTime;
    public String note;

    public AppointmentRequest(String doctorId, String appointmentTime, String note) {
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.note = note;
    }
}
