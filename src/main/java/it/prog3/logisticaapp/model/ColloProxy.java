package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.database.GestoreDatabase;
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

    private static final long serialVersionUID = 1L;

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
                // Se non troviamo i dettagli, potrebbe essere un errore grave di consistenza DB
                throw new RuntimeException("Errore Data Integrity: Collo " + codice + " esiste nell'indice ma non nei dettagli.");
            }
        }
        return this.colloReale;
    }

    // =========================================================================
    // IMPLEMENTAZIONE ICollo (Metodi Proxy)
    // =========================================================================

    @Override
    public String getCodice() {
        return this.codice;
    }

    @Override
    public void setCodice(String codice) {
        this.codice = codice; // Aggiorna locale
        if (colloReale != null) {
            colloReale.setCodice(codice); // Aggiorna reale se esiste
        }
    }

    @Override
    public String getStato() {
        // Se il reale è già in memoria, usa quello (potrebbe essere più fresco)
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
            throw new SecurityException("Utente " + ruolo + " non autorizzato a modificare lo stato.");
        }

        // 2. Aggiornamento
        this.stato = nuovoStato; // Correggi qui: usa 'nuovoStato', non 'stato'
        if (this.colloReale != null) {
            this.colloReale.setStato(nuovoStato);
        }
    }

    // --- Metodi che forzano il caricamento (Virtual Proxy / Delegation) ---

    @Override
    public void aggiungiEventoStorico(String evento) {
        // Controllo sicurezza: Solo il corriere o il sistema dovrebbero scrivere nello storico
        if (Sessione.getInstance().getRuoloCorrente() != Sessione.Ruolo.CORRIERE &&
                Sessione.getInstance().getRuoloCorrente() != Sessione.Ruolo.MANAGER) {
            // Nota: A volte il manager può forzare note, altrimenti lascia solo CORRIERE
        }

        // Delega al reale (caricandolo se serve)
        getColloReale().aggiungiEventoStorico(evento);
    }

    // IL METODO CHE MANCAVA
    @Override
    public void setStorico(List<String> storico) {
        // Delega completamente all'oggetto reale
        getColloReale().setStorico(storico);
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

    @Override
    public String toString() {
        return codice + " (" + stato + ") [Proxy]";
    }
}