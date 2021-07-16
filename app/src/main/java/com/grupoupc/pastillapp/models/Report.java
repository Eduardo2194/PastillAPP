package com.grupoupc.pastillapp.models;

public class Report implements Comparable<Report> {
    String fecvisita;
    String cantvisita;

    public Report(String fecvisita, String cantvisita) {
        this.fecvisita = fecvisita;
        this.cantvisita = cantvisita;
    }

    public String getFecvisita() {
        return fecvisita;
    }

    public void setFecvisita(String fecvisita) {
        this.fecvisita = fecvisita;
    }

    public String getCantvisita() {
        return cantvisita;
    }

    public void setCantvisita(String cantvisita) {
        this.cantvisita = cantvisita;
    }

    // compareTo for sorting
    @Override
    public int compareTo(Report other) {
        // Ordenamos nuestra lista por fecha
        return this.fecvisita.compareTo(other.fecvisita);
    }
}
