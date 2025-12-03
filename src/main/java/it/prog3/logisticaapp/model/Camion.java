package it.prog3.logisticaapp.model;

public class Camion extends VeicoloAstratto {

    // Costruttore vuoto (JavaBean)
    public Camion() {
        super();
        this.setCapienza(100); // Default per Camion vuoti
    }

    // Costruttore usato dalla Factory
    public Camion(String codice, String azienda) {
        // Passa 100 come capienza fissa per i Camion
        super(codice, 100, azienda);
    }
}
