package com.grupoupc.pastillapp.models;

public class User {
    String id, username, email, photo, type;

    public User() {

    }

    public User(String id, String username, String email, String photo, String type) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.photo = photo;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoto() {
        return photo;
    }

    public String getType() {
        return type;
    }
}