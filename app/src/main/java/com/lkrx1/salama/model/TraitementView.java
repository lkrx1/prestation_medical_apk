package com.lkrx1.salama.model;

public class TraitementView {
    public int id;
    public String nom_medecin;
    public String nom_patient;
    public Double prestations;
    public int nb_jour;

    public TraitementView() {
    }

    public TraitementView(int id, String nom_medecin, String nom_patient, Double prestations, int nb_jour) {
        this.id = id;
        this.nom_medecin = nom_medecin;
        this.nom_patient = nom_patient;
        this.prestations = prestations;
        this.nb_jour = nb_jour;
    }
}
