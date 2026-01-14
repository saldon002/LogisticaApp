package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.database.IDataLoader;
import java.util.List;

/**
 * Proxy che gestisce l'accesso all'oggetto {@link ColloReale}.
 * <p>
 * Implementa l'interfaccia composita {@link ICollo} e combina due pattern:
 * 1. <b>Virtual Proxy:</b> Lazy Loading dei dati pesanti (storico/dettagli).
 * 2. <b>Protection Proxy:</b> Controllo accessi basato sui ruoli della Sessione.
 * </p>
 */
public class ColloProxy implements ICollo {

    // Riferimento all'oggetto reale (Lazy)
    private ColloReale colloReale;

    // Dati "leggeri"
    private String codice;
    private String stato;

    // Dipendenza per il caricamento (Dependency Injection)
    private IDataLoader dataLoader;

    /**
     * Costruttore con Dependency Injection.
     * @param loader Chi si occupa di recuperare i dati (es. GestoreDatabase)
     */
    public ColloProxy(String codice, String stato, IDataLoader loader) {
        this.codice = codice;
        this.stato = stato;
        this.dataLoader = loader;
        this.colloReale = null;
    }

    private ColloReale getColloReale() {
        if (this.colloReale == null) {
            // Lazy Loading tramite l'interfaccia iniettata
            if (this.dataLoader == null) {
                throw new RuntimeException("DataLoader non configurato nel Proxy!");
            }
            this.colloReale = this.dataLoader.getColloRealeCompleto(this.codice);
        }
        return this.colloReale;
    }

    // ==========================================
    // PROTECTION PROXY & DELEGA
    // ==========================================

    private void checkPermessiScrittura() {
        if (Sessione.getInstance().getRuoloCorrente() == Sessione.Ruolo.CLIENTE) {
            throw new SecurityException("Accesso Negato: I clienti non possono modificare i dati.");
        }
    }

    @Override
    public String getCodice() { return this.codice; }

    @Override
    public void setCodice(String codice) {
        this.codice = codice;
        if (colloReale != null) colloReale.setCodice(codice);
    }

    @Override
    public String getStato() {
        if (colloReale != null) return colloReale.getStato();
        return stato;
    }

    @Override
    public void setStato(String stato) {
        checkPermessiScrittura();
        getColloReale().setStato(stato);
        this.stato = stato; // Allineiamo il dato locale
    }

    @Override
    public void setMittente(String mittente) {
        checkPermessiScrittura();
        getColloReale().setMittente(mittente);
    }

    @Override
    public void setDestinatario(String destinatario) {
        checkPermessiScrittura();
        getColloReale().setDestinatario(destinatario);
    }

    @Override
    public void setPeso(double peso) {
        checkPermessiScrittura();
        getColloReale().setPeso(peso);
    }

    @Override
    public double getPeso() { return getColloReale().getPeso(); }

    @Override
    public String getMittente() { return getColloReale().getMittente(); }

    @Override
    public String getDestinatario() { return getColloReale().getDestinatario(); }

    @Override
    public List<String> getStorico() { return getColloReale().getStorico(); }

    @Override
    public void setStorico(List<String> storico) { getColloReale().setStorico(storico); }

    @Override
    public void aggiungiEventoStorico(String evento) {
        // Se volessi, potresti mettere checkPermessiScrittura() anche qui
        getColloReale().aggiungiEventoStorico(evento);
    }

    @Override
    public String toString() {
        return codice + " (" + stato + ") [Proxy]";
    }
}