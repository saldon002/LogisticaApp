package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.database.ConnessioneDB;
import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.util.Observer;
import java.util.List;

/**
 * Classe Proxy che gestisce l'accesso all'oggetto {@link ColloReale}
 * <p>
 *     Implementa due varianti del pattern Proxy:
 *     1. <b>Virtual Proxy:</b> Carica i dati pesanti (storico) dal DB solo su richiesta.
 *     2. <b>Protection Proxy:</b> Controlla i permessi prima di permettere modifiche allo stato.
 * </p>
 */
public class ColloProxy implements ICollo{
    // Riferimento all'oggetto reale. Inizialmente è null
    private ColloReale colloReale;

    // Dati "leggeri" che il Proxy possiede subito
    private String codice;
    private String stato;

    // Attributo per simulare il ruolo dell'utente corrente
    private String ruoloUtenteCorrente;

    /**
     * @param codice Il codice univoco del collo
     * @param stato Lo stato attuale
     */
    public ColloProxy(String codice, String stato){
        this.codice = codice;
        this.stato = stato;
        this.colloReale = null;

        this.ruoloUtenteCorrente = "CLIENTE";
    }

    /**
     * Metodo helper privato che carica l'oggetto reale se non esiste.
     */
    private ColloReale getColloReale(){
        if(this.colloReale==null){
            System.out.println("[Proxy] Caricamento dati dal DB per collo: " + codice);

            GestoreDatabase db = new GestoreDatabase();
            this.colloReale = db.getColloRealeCompleto(this.codice);

            // Se il DB non trova nulla
            if (this.colloReale==null){
                throw new RuntimeException("Errore critico: Collo " + codice + " non trovato nel database.");
            }
        }
        return this.colloReale;
    }


    // Implementazione Metodi ICollo
    @Override
    public String getCodice() {return this.codice;}
    @Override
    public void setCodice(String codice) {
        this.codice = codice;
        if (colloReale != null) colloReale.setCodice(codice);
    }
    @Override
    public String getStato() {
        // Se ho il reale uso quello (più aggiornato), altrimenti uso il mio dato leggero
        if (colloReale != null) return colloReale.getStato();
        return stato;
    }

    /**
     * Implementazione del PROTECTION PROXY.
     * Solo i CORRIERI possono cambiare lo stato.
     */
    @Override
    public void setStato(String nuovoStato) {
        if (!"CORRIERE".equalsIgnoreCase(ruoloUtenteCorrente)) {
            throw new SecurityException("Accesso Negato: Solo i corrieri possono modificare lo stato");

            // utente autorizzato
            getColloReale().setStato(nuovoStato);

            this.stato = nuovoStato;
        }
    }

    @Override
    public List<String> getStorico() {
        // Questa è l'operazione "pesante". Chiamo getWrapee() che scatena il caricamento dal DB.
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


    // --- Deleghe per il Pattern Observer ---
    // Poiché ColloReale estende Subject, dobbiamo delegare le registrazioni a lui.
    // Nota: questo forza il caricamento del reale, perché è lui che deve tenere la lista.

    // ATTENZIONE: Se ICollo non ha i metodi attach/detach (perché Subject non è interfaccia)
    // dobbiamo fare casting o gestire la cosa diversamente.
    // Nel passo precedente abbiamo detto che ICollo estende Serializable.
    // Per far funzionare l'Observer tramite Proxy, ICollo dovrebbe esporre metodi per l'observer
    // OPPURE il Controller deve osservare direttamente il Reale dopo averlo ottenuto.
    // Per semplicità e coerenza col diagramma (Proxy implementa ICollo), assumiamo che
    // il caricamento avvenga trasparentemente.
}
