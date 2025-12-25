package com.example.umc.models;


public class User {
    private String  id;
    private String name;
    private String email;
    private String phone;
    private String birthday;
    private String gender;
    private String avatarUrl;
    private String role ;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBirthday() { return birthday; }
    public String getGender() { return gender; }
    public String getAvatarUrl() { return avatarUrl; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
