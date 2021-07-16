package com.grupoupc.pastillapp.models;

public class Pharmacy {
    String id, name, address, latitude, longitude, phone, open, close, photo;

    public Pharmacy() {

    }

    public Pharmacy (String id, String name, String address, String latitude, String longitude, String phone, String open, String close, String photo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.open = open;
        this.close = close;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPhone() {
        return phone;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public String getPhoto() {
        return photo;
    }
}