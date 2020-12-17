package com.tugasbesardua.models;

public class UserData {
    String name;
    String email;
    String phone;
    String city;
    String photoUrl;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public UserData() {}

    public UserData(String name, String email, String phone, String city, String photoUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.photoUrl = photoUrl;
    }
}
