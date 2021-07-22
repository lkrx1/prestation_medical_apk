package com.lkrx1.salama.model;

public class Traitement {
    private int id;
    private int id_medecin;
    private int id_patient;
    private int nb_jour;

    public Traitement() {
    }

    public Traitement(int id_medecin, int id_patient, int nb_jour) {
        this.id_medecin = id_medecin;
        this.id_patient = id_patient;
        this.nb_jour = nb_jour;
    }

    public Traitement(int id, int id_medecin, int id_patient, int nb_jour) {
        this.id = id;
        this.id_medecin = id_medecin;
        this.id_patient = id_patient;
        this.nb_jour = nb_jour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public int getId_patient() {
        return id_patient;
    }

    public void setId_patient(int id_patient) {
        this.id_patient = id_patient;
    }

    public int getNb_jour() {
        return nb_jour;
    }

    public void setNb_jour(int nb_jour) {
        this.nb_jour = nb_jour;
    }
}
