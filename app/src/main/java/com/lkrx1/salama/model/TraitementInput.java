package com.lkrx1.salama.model;

public class TraitementInput {
    private int id;
    private Medecin medecin;
    private Patient patient;
    private int nbjour;

    public TraitementInput() {
    }

    public TraitementInput(int id, Medecin medecin, Patient patient, int nbjour) {
        this.id = id;
        this.medecin = medecin;
        this.patient = patient;
        this.nbjour = nbjour;
    }

    public TraitementInput(Medecin medecin, Patient patient, int nbjour) {
        this.medecin = medecin;
        this.patient = patient;
        this.nbjour = nbjour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public int getNbjour() {
        return nbjour;
    }

    public void setNbjour(int nbjour) {
        this.nbjour = nbjour;
    }
}
