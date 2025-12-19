package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.util.Observer;
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

    // Riferimento all'oggetto reale (Lazy).
    private ColloReale colloReale;

    // Dati "leggeri" mantenuti nel Proxy per evitare query inutili.
    private String codice;
    private String stato;

    /**
     * Costruttore leggero.
     * Non effettua connessioni al DB.
     */
    public ColloProxy(String codice, String stato) {
        this.codice = codice;
        this.stato = stato;
        this.colloReale = null;
    }

    /**
     * Lazy Loading: Carica l'oggetto reale solo quando serve.
     */
    private ColloReale getColloReale() {
        if (this.colloReale == null) {
            System.out.println("[Proxy] Lazy Loading: Recupero dati completi per " + codice + "...");

            // Usiamo il GestoreDatabase per creare l'oggetto
            GestoreDatabase db = new GestoreDatabase();
            this.colloReale = db.getColloRealeCompleto(this.codice);

            // Controllo robustezza
            if (this.colloReale == null) {
                throw new RuntimeException("Errore Data Integrity: Collo " + codice + " esiste nell'indice ma non nei dettagli.");
            }
        }
        return this.colloReale;
    }

    // =========================================================================
    // IMPLEMENTAZIONE IColloDati (Business)
    // =========================================================================

    @Override
    public String getCodice() {
        return this.codice;
    }

    @Override
    public void setCodice(String codice) {
        this.codice = codice; // Aggiorna locale
        if (colloReale != null) colloReale.setCodice(codice); // Aggiorna reale se esiste
    }

    @Override
    public String getStato() {
        // Se il reale è già in memoria, usa quello che è sicuramente più aggiornato.
        // Altrimenti restituisci quello che sai tu (cache leggera).
        if (colloReale != null) return colloReale.getStato();
        return stato;
    }

    /**
     * PROTECTION PROXY: Controlla i permessi prima di scrivere.
     */
    @Override
    public void setStato(String nuovoStato) {
        // 1. Controllo Sicurezza (Protection)
        Sessione.Ruolo ruolo = Sessione.getInstance().getRuoloCorrente();

        if (ruolo == Sessione.Ruolo.CLIENTE) {
            // Lancia l'eccezione prevista dal contratto (LSP compliant)
            throw new SecurityException("Utente " + ruolo + " non autorizzato a modificare lo stato.");
        }

        this.stato = stato;
        if (this.colloReale != null) {
            getColloReale().setStato(stato);
        }
    }

    // --- Metodi che forzano il caricamento (Virtual Proxy) ---

    @Override
    public void aggiungiEventoStorico(String evento) {
        // Controllo sicurezza
        if (Sessione.getInstance().getRuoloCorrente() != Sessione.Ruolo.CORRIERE) {
            throw new SecurityException("Utente non autorizzato a modificare lo storico.");
        }
        // Delega al reale
        getColloReale().aggiungiEventoStorico(evento);
    }

    @Override
    public List<String> getStorico() {
        return getColloReale().getStorico();
    }

    @Override
    public String getMittente() { return getColloReale().getMittente(); }
    @Override
    public void setMittente(String m) { getColloReale().setMittente(m); }

    @Override
    public String getDestinatario() { return getColloReale().getDestinatario(); }
    @Override
    public void setDestinatario(String d) { getColloReale().setDestinatario(d); }

    @Override
    public double getPeso() { return getColloReale().getPeso(); }
    @Override
    public void setPeso(double p) { getColloReale().setPeso(p); }

}