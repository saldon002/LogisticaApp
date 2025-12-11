package it.prog3.logisticaapp.model;

public class Camion extends VeicoloAstratto {

    // Costruttore vuoto (JavaBean)
    public Camion() {
        super();
        this.setCapienza(100); // Default per Camion vuoti
    }

    // Costruttore usato dalla Factory
    public Camion(String codice) {
        super(codice, 100);
    }

    @Override
    public String getTipo() {
        return "CAMION";
    }
}
