package it.prog3.logisticaapp.model;

public class Furgone extends VeicoloAstratto {

    public Furgone() {
        super();
        this.setCapienza(20);
    }

    public Furgone(String codice) {
        super(codice, 20);
    }

    @Override
    public String getTipo() {
        return "FURGONE";
    }
}
