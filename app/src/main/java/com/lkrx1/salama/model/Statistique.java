package com.lkrx1.salama.model;

public class Statistique {
    private int id;
    private Medecin medecin;
    private int effectif;
    private Double percent;

    public Statistique() {
    }

    public Statistique(Medecin medecin, int effectif, Double percent) {
        this.medecin = medecin;
        this.effectif = effectif;
        this.percent = percent;
    }

    public Statistique(int id, Medecin medecin, int effectif, Double percent) {
        this.id = id;
        this.medecin = medecin;
        this.effectif = effectif;
        this.percent = percent;
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

    public int getEffectif() {
        return effectif;
    }

    public void setEffectif(int effectif) {
        this.effectif = effectif;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}
