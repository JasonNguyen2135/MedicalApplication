package com.example.umc.models;

public class Doctor {

    private String id;        // 🔥 Firebase documentId
    private String name;
    private String speciality;
    private int experience;
    private String imageUrl;

    private Double ratingAvg;
    private Long ratingCount;
    private WorkSchedule workSchedule;

    public Double getRatingAvg() {
        return ratingAvg;
    }

    public Long getRatingCount() {
        return ratingCount;
    }
    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }
    public void setRatingAvg(Double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }
    public Doctor() {} // BẮT BUỘC cho Firebase
    public WorkSchedule getWorkSchedule() {
        return workSchedule;
    }
    public void setWorkSchedule(WorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
    }

    // ===== ID =====
    public String getId() {
        return id;
    }

    public void setId(String id) {   // 🔥 String
        this.id = id;
    }

    // ===== OTHER FIELDS =====
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }



}
