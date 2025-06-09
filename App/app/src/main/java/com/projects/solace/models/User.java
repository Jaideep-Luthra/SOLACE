package com.projects.solace.models;


public class User {
    public String name;
    public String email;
    public String userType;
    public String token;

    public User() {
    }

    public User(String name, String email, String userType, String token) {
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }

    public String getToken() {
        return token;
    }
}
