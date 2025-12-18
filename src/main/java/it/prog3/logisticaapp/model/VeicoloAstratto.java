package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che implementa la logica comune a tutti i veicoli.
 */
public abstract class VeicoloAstratto extends Subject implements IVeicolo {

    protected String codice;
    protected int capienza;
    protected List<ICollo> carico;

    public VeicoloAstratto() {
        this.carico = new ArrayList<>();
    }

    public VeicoloAstratto(String codice, int capienza) {
        this();
        setCodice(codice);
        setCapienza(capienza);
    }

    @Override
    public boolean caricaCollo(ICollo collo) {
        if (collo == null) {
            throw new IllegalArgumentException("Impossibile caricare un collo nullo.");
        }
        // Controllo capienza
        if (carico.size() < capienza) {
            carico.add(collo);
            notifyObservers();
            return true;
        }
        return false;
    }

    @Override
    public List<ICollo> getCarico() { return carico; }

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

    @Override
    public String toString() {
        return getTipo() + " [targa=" + codice + ", carico=" + carico.size() + "/" + capienza + "]";
    }
}