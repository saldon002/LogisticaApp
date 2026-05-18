package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

public class Azienda {

    private String nome;
    private List<IVeicolo> flotta;

    public Azienda(String nome) {
        this.nome = nome;
        this.flotta = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public List<IVeicolo> getFlotta() { return flotta; }
    public void setFlotta(List<IVeicolo> flotta) {
        this.flotta = (flotta != null) ? flotta : new ArrayList<>();
    }

    /**
     * Aggiunge un veicolo già creato (tramite Factory) alla flotta.
     */
    public void aggiungiVeicolo(IVeicolo v) {
        if (v != null) {
            flotta.add(v);
        }
    }

    @Override
    public String toString() {
        return nome + " (" + flotta.size() + " veicoli)";
    }
}