package com.lkrx1.salama.model;

public class Medecin {
    int id;
    String nom;
    Double taux_journalier;

    public Medecin() {
    }

    public Medecin(String nom, Double taux_journalier) {
        this.nom = nom;
        this.taux_journalier = taux_journalier;
    }

    public Medecin(int id, String nom, Double taux_journalier) {
        this.id = id;
        this.nom = nom;
        this.taux_journalier = taux_journalier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getTaux_journalier() {
        return taux_journalier;
    }

    public void setTaux_journalier(Double taux_journalier) {
        this.taux_journalier = taux_journalier;
    }
}
