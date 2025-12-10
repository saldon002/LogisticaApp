package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.business.Sessione; // Importiamo la Sessione per i ruoli
import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.util.Observer;
import java.util.List;

/**
 * Classe Proxy che gestisce l'accesso all'oggetto {@link ColloReale}.
 * <p>
 * Implementa due varianti del pattern Proxy:
 * 1. <b>Virtual Proxy:</b> Carica i dati pesanti (storico) dal DB solo su richiesta.
 * 2. <b>Protection Proxy:</b> Controlla i permessi (tramite Sessione) prima di permettere modifiche.
 * </p>
 */
public class ColloProxy implements ICollo {

    // Riferimento all'oggetto reale. Inizialmente è null (Lazy Loading).
    private ColloReale colloReale;

    // Dati "leggeri" che il Proxy possiede subito.
    private String codice;
    private String stato;

    /**
     * Costruttore del Proxy.
     * @param codice Il codice univoco del collo.
     * @param stato Lo stato attuale.
     */
    public ColloProxy(String codice, String stato) {
        this.codice = codice;
        this.stato = stato;
        this.colloReale = null;
    }

    /**
     * Metodo helper privato che carica l'oggetto reale se non esiste.
     */
    private ColloReale getColloReale() {
        if (this.colloReale == null) {
            System.out.println("[Proxy] Caricamento dati pesanti dal DB per collo: " + codice);

            GestoreDatabase db = new GestoreDatabase();
            this.colloReale = db.getColloRealeCompleto(this.codice);

            // Se il DB non trova nulla
            if (this.colloReale == null) {
                throw new RuntimeException("Errore critico: Collo " + codice + " non trovato nel database.");
            }
        }
        return this.colloReale;
    }

    // --- Implementazione Metodi ICollo ---

    @Override
    public String getCodice() {
        return this.codice;
    }

    @Override
    public void setCodice(String codice) {
        this.codice = codice;
        if (colloReale != null) colloReale.setCodice(codice);
    }

    @Override
    public String getStato() {
        // Se ho il reale caricato uso quello (più fresco), altrimenti uso il dato locale
        if (colloReale != null) return colloReale.getStato();
        return stato;
    }

    /**
     * Implementazione del PROTECTION PROXY.
     * Controlla la Sessione corrente: solo CORRIERE o MANAGER possono modificare.
     */
    @Override
    public void setStato(String nuovoStato) {
        // 1. Recupero il ruolo dalla Sessione globale
        Sessione.Ruolo ruolo = Sessione.getInstance().getRuoloCorrente();

        // 2. Controllo i permessi
        if (ruolo != Sessione.Ruolo.CORRIERE && ruolo != Sessione.Ruolo.MANAGER) {
            throw new SecurityException("ACCESSO NEGATO: L'utente " + ruolo + " non può modificare lo stato.");
        }

        // 3. Se autorizzato, delego al reale (caricandolo se serve)
        getColloReale().setStato(nuovoStato);

        // 4. Aggiorna anche il dato locale
        this.stato = nuovoStato;
    }

    // --- Deleghe semplici (Virtual Proxy) ---

    @Override
    public List<String> getStorico() {
        // Scatena il caricamento dal DB
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


    // --- Deleghe per il Pattern Observer (OBBLIGATORIE per ICollo) ---

    @Override
    public void attach(Observer o) {
        // Se qualcuno vuole osservare il Proxy, in realtà osserva il Reale.
        // Questo forza il caricamento, perché la lista sta dentro il Reale.
        getColloReale().attach(o);
    }

    @Override
    public void detach(Observer o) {
        getColloReale().detach(o);
    }

    @Override
    public void notifyObservers() {
        getColloReale().notifyObservers();
    }
}