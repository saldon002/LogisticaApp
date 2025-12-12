package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.Observer;
import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per un'entità di tipo "Collo".
 * <p>
 * Estende {@link Serializable} per conformità alle specifiche JavaBean/JDBC.
 * Questa interfaccia unifica le responsabilità di:
 * 1. Data Carrier (metodi getter/setter).
 * 2. Subject (pattern Observer) per permettere al Proxy di delegare la registrazione.
 * </p>
 */
public interface ICollo extends Serializable {

    /**
     * Restituisce il codice univoco del collo.
     * @return Stringa rappresentante il codice.
     */
    String getCodice();
    void setCodice(String codice);

    /**
     * Restituisce lo stato attuale del collo.
     * @return Stringa rappresentante lo stato (es. "IN_MAGAZZINO", "SPEDITO").
     */
    String getStato();

    /**
     * Imposta il nuovo stato del collo.
     * <p>
     * <b>Nota sul Liskov Substitution Principle:</b>
     * Le implementazioni possono variare il comportamento:
     * - {@link ColloReale}: Aggiorna lo stato e notifica gli observer.
     * - {@link ColloProxy}: Può bloccare l'operazione se l'utente non ha i permessi.
     * </p>
     *
     * @param stato Il nuovo stato da assegnare.
     * @throws SecurityException (Unchecked) Se l'implementazione è un Proxy e l'utente non è autorizzato.
     */
    void setStato(String stato);

    String getMittente();
    void setMittente(String mittente);

    String getDestinatario();
    void setDestinatario(String destinatario);

    double getPeso();

    /**
     * Imposta il peso del collo.
     * @param peso Il peso in kg.
     * @throws IllegalArgumentException Se il peso è negativo.
     */
    void setPeso(double peso);

    /**
     * Recupera lo storico degli spostamenti del collo.
     * @return Lista di stringhe descrittive dello storico.
     * @throws RuntimeException Se si verifica un errore nel recupero dati (es. DB offline nel Proxy).
     */
    List<String> getStorico();

    // =========================================================================
    // METODI PATTERN OBSERVER
    // =========================================================================

    /**
     * Registra un osservatore interessato ai cambiamenti di stato.
     * @param o L'osservatore da registrare.
     * @throws IllegalArgumentException Se l'observer è null.
     */
    void attach(Observer o);

    /**
     * Rimuove un osservatore dalla lista.
     * @param o L'osservatore da rimuovere.
     */
    void detach(Observer o);

    /**
     * Notifica tutti gli osservatori registrati.
     * Solitamente chiamato internamente da setStato(), ma esposto per coerenza tra Proxy e Reale.
     */
    void notifyObservers();
}