    package com.example.umc.dto;

    public class PaymentRequest {
        public String appointmentId;
        public long amount;
        public PaymentRequest(String appointmentId, long amount)
        {this.appointmentId=appointmentId;this.amount=amount;}
    }