package it.prog3.logisticaapp.model;

public class Furgone extends VeicoloAstratto {

    // Costante per la capienza standard (opzionale ma pulita)
    private static final int CAPIENZA_STANDARD = 20;

    public Furgone() {
        super();
        // Usiamo il setter per attivare eventuali validazioni del padre
        this.setCapienza(CAPIENZA_STANDARD);
    }

    public Furgone(String codice) {
        super(codice, CAPIENZA_STANDARD);
    }

    @Override
    public String getTipo() {
        return "FURGONE";
    }
}