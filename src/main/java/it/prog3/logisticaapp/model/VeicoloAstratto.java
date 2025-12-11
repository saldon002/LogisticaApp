package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che implementa la logica comune a tutti i veicoli.
 * <p>
 * Gestisce gli attributi base e la lista del carico.
 * Rimuove la duplicazione di codice (DRY) dalle classi concrete Camion e Furgone.
 * </p>
 */
public abstract class VeicoloAstratto implements IVeicolo {

    protected String codice;
    protected int capienza;

    // NOTA: Abbiamo rimosso 'azienda' per rispettare rigorosamente la traccia.
    // L'appartenenza è data dal fatto che questo oggetto starà nella lista dell'Azienda.

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
     * @param codice La targa o identificativo.
     * @param capienza Il numero massimo di colli.
     */
    public VeicoloAstratto(String codice, int capienza) {
        this(); // Chiama il costruttore vuoto per init lista
        this.codice = codice;
        this.capienza = capienza;
    }

    // --- Implementazione Logica Comune ---

    @Override
    public boolean carica(ICollo collo) {
        if (collo == null) return false;

        // Controllo semplice sulla capienza (numero di colli)
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

    // NOTA: Non implementiamo getTipo() qui.
    // Lo faranno le classi concrete (Camion/Furgone) ritornando la stringa fissa.

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [targa=" + codice + ", carico=" + carico.size() + "/" + capienza + "]";
    }
}