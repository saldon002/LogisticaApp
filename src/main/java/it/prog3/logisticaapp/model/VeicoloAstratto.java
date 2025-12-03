package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che implementa la logica comune a tutti i veicoli.
 * <p>
 * Gestisce gli attributi base e la lista del carico.
 * </p>
 */
public abstract class VeicoloAstratto implements IVeicolo {
    protected String codice;
    protected int capienza;
    protected String azienda;

    // Aggregazione: il veicolo contiene i colli
    protected List<ICollo> carico;

    /**
     * Costruttore vuoto (JavaBean).
     * Inizializza la lista per evitare NullPointerException.
     */
    public VeicoloAstratto() {
        this.carico = new ArrayList<>();
    }

    /**
     * Costruttore parametrico per le sottoclassi.
     */
    public VeicoloAstratto(String codice, int capienza, String azienda) {
        this();
        this.codice = codice;
        this.capienza = capienza;
        this.azienda = azienda;
    }

    // --- Implementazione Logica Comune ---

    @Override
    public boolean carica(ICollo collo) {
        if (collo == null) return false;

        // Controllo semplice sulla capienza (numero di colli)
        // Nota: La traccia dice "capienza container (numero di colli)"
        if (carico.size() < capienza) {
            carico.add(collo);
            return true;
        }
        return false;
    }

    @Override
    public List<ICollo> getCarico() {
        return carico;
    }

    // --- Getters e Setters Standard ---

    @Override
    public String getCodice() { return codice; }

    @Override
    public void setCodice(String codice) { this.codice = codice; }

    @Override
    public int getCapienza() { return capienza; }

    @Override
    public void setCapienza(int capienza) { this.capienza = capienza; }

    @Override
    public String getAzienda() { return azienda; }

    @Override
    public void setAzienda(String azienda) { this.azienda = azienda; }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [targa=" + codice + ", carico=" + carico.size() + "/" + capienza + "]";
    }
}
