package com.grupoupc.pastillapp.models;

public class Available {
    String pharmacy_id, pharmacy_name, product_price;

    public Available() {

    }

    public Available(String pharmacy_id, String pharmacy_name, String product_price) {
        this.pharmacy_id = pharmacy_id;
        this.pharmacy_name = pharmacy_name;
        this.product_price = product_price;
    }

    public String getPharmacy_id() {
        return pharmacy_id;
    }

    public String getPharmacy_name() {
        return pharmacy_name;
    }

    public String getProduct_price() {
        return product_price;
    }
}
