package com.grupoupc.pastillapp.models;

public class Product {
    String id, name, presentation, category, prescription, register_date, description, photo;

    public Product() {

    }

    public Product(String id, String name, String presentation, String category, String prescription, String register_date, String description, String photo) {
        this.id = id;
        this.name = name;
        this.presentation = presentation;
        this.category = category;
        this.prescription = prescription;
        this.register_date = register_date;
        this.description = description;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPresentation() {
        return presentation;
    }

    public String getCategory() {
        return category;
    }

    public String getPrescription() {
        return prescription;
    }

    public String getRegister_date() {
        return register_date;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }
}