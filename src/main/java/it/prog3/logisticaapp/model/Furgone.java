package it.prog3.logisticaapp.model;

public class Furgone extends VeicoloAstratto {

    public Furgone() {
        super();
        this.setCapienza(20);
    }

    public Furgone(String codice, String azienda) {
        // Passa 20 come capienza fissa per i Furgoni
        super(codice, 20, azienda);
    }
}
