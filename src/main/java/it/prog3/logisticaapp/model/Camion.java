package it.prog3.logisticaapp.model;

public class Camion extends VeicoloAstratto {

    private static final int CAPIENZA_STANDARD = 5;

    public Camion(String codice) {
        super(codice, CAPIENZA_STANDARD);
    }

    @Override
    public String getTipo() {
        return "CAMION";
    }
}