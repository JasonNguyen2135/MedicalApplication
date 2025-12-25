package com.example.umc.models;

import com.google.firebase.Timestamp;

public class Appointment {

    private String id;
    private String userId;
    private String doctorId;

    // 🔥 SNAPSHOT THÔNG TIN BÁC SĨ (DÙNG CHO UI)
    private String userName;
    private String doctorName;
    private String doctorSpeciality;

    private String appointmentTime;
    private Timestamp appointmentAt;
    private String note;
    private String status;
    private Boolean paid;
    private Double price;
    private Long holdUntil;

    private String affectedReason;   // DOCTOR_DELETED / DOCTOR_TEMP_LEAVE
    private Double refundAmount;

    private Integer rating;
    private String review;
    private String baseStatus;
    private String paymentCode;
    private Long approveUntil;
    private Long paidAt;
    private String paymentMethod;
    private String paymentContent;
    private String userEmail;
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPaymentCode() { return paymentCode; }
    public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
    public String getPaymentContent() { return paymentContent; }
    public void setPaymentContent(String paymentContent) { this.paymentContent = paymentContent; }
    public Long getApproveUntil() { return approveUntil; }
    public void setApproveUntil(Long approveUntil) { this.approveUntil = approveUntil; }

    public Long getPaidAt() { return paidAt; }
    public void setPaidAt(Long paidAt) { this.paidAt = paidAt; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }


    public Appointment() {}

    // ===== SNAPSHOT DOCTOR =====
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpeciality() { return doctorSpeciality; }
    public void setDoctorSpeciality(String doctorSpeciality) {
        this.doctorSpeciality = doctorSpeciality;
    }

    // ===== GET / SET =====
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPaid() { return paid != null && paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Long getHoldUntil() { return holdUntil; }
    public void setHoldUntil(Long holdUntil) { this.holdUntil = holdUntil; }

    public String getAffectedReason() { return affectedReason; }
    public void setAffectedReason(String affectedReason) { this.affectedReason = affectedReason; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getBaseStatus() { return baseStatus; }
    public void setBaseStatus(String baseStatus) { this.baseStatus = baseStatus; }
}
