package it.prog3.logisticaapp.model;

public class Furgone extends VeicoloAstratto {

    private static final int CAPIENZA_STANDARD = 2;

    public Furgone(String codice) {
        super(codice, CAPIENZA_STANDARD);
    }

    @Override
    public String getTipo() {
        return "FURGONE";
    }
}