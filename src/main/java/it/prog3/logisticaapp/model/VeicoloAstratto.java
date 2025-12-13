package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che implementa la logica comune a tutti i veicoli.
 * <p>
 * Implementa il pattern <b>Template Method</b> implicito: fornisce l'implementazione base
 * per la gestione del carico, lasciando alle sottoclassi solo la definizione del tipo.
 * Rispetta il principio DRY (Don't Repeat Yourself).
 * </p>
 */
public abstract class VeicoloAstratto implements IVeicolo {

    protected String codice;
    protected int capienza;

    // Aggregazione: il veicolo contiene i colli.
    // Usiamo l'interfaccia ICollo per rispettare il DIP (Dependency Inversion).
    protected List<ICollo> carico;

    /**
     * Costruttore vuoto.
     * Inizializza la lista per evitare NullPointerException.
     */
    public VeicoloAstratto() {
        this.carico = new ArrayList<>();
    }

    /**
     * Costruttore parametrico.
     * @param codice La targa o identificativo.
     * @param capienza Il numero massimo di colli.
     */
    public VeicoloAstratto(String codice, int capienza) {
        this();
        setCodice(codice);   // Usiamo i setter per validare
        setCapienza(capienza);
    }

    // --- Implementazione Logica Comune ---

    @Override
    public boolean carica(ICollo collo) {
        // Controllo robustezza: Il null non è un caso di "veicolo pieno", è un errore.
        if (collo == null) {
            throw new IllegalArgumentException("Impossibile caricare un collo nullo.");
        }

        // Controllo capienza
        if (carico.size() < capienza) {
            carico.add(collo);
            return true; // Caricato con successo
        }

        return false; // Veicolo pieno
    }

    @Override
    public List<ICollo> getCarico() {
        return carico;
    }

    // --- Getters e Setters con Validazione (LSP Compliance) ---

    @Override
    public String getCodice() { return codice; }

    @Override
    public void setCodice(String codice) {
        if (codice == null || codice.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice del veicolo non può essere vuoto.");
        }
        this.codice = codice;
    }

    @Override
    public int getCapienza() { return capienza; }

    @Override
    public void setCapienza(int capienza) {
        if (capienza <= 0) {
            throw new IllegalArgumentException("La capienza deve essere maggiore di zero.");
        }
        this.capienza = capienza;
    }

    // NOTA: getTipo() rimane astratto (da implementare nelle sottoclassi)

    @Override
    public String toString() {
        return getTipo() + " [targa=" + codice + ", carico=" + carico.size() + "/" + capienza + "]";
    }
}