package it.prog3.logisticaapp.business;

/**
 * Gestisce la sessione utente corrente tramite il pattern Singleton.
 * <p>
 * Questa classe permette di simulare il login di diversi attori (Manager, Corriere, Cliente)
 * mantenendo lo stato globale dell'applicazione.
 * Rispetta il principio SRP gestendo unicamente i dati di sessione.
 * </p>
 */
public class Sessione {

    private static Sessione instance;

    // Definizione dei ruoli nel sistema
    public enum Ruolo {
        MANAGER,
        CORRIERE,
        CLIENTE
    }

    private Ruolo ruoloCorrente;

    // Costruttore privato per impedire istanziazione esterna
    private Sessione() {
        // Default: Il cliente è l'attore con permessi minimi
        this.ruoloCorrente = Ruolo.CLIENTE;
        System.out.println("[Sessione] Inizializzata con ruolo default: CLIENTE");
    }

    /**
     * Restituisce l'istanza unica della Sessione (Lazy Initialization).
     * @return L'oggetto Sessione.
     */
    public static Sessione getInstance() {
        if (instance == null) {
            instance = new Sessione();
        }
        return instance;
    }

    /**
     * Restituisce il ruolo dell'utente corrente.
     * @return Il valore dell'Enum Ruolo (mai null).
     */
    public Ruolo getRuoloCorrente() {
        return ruoloCorrente;
    }

    /**
     * Permette di cambiare il ruolo corrente (Simulazione Login).
     * @param ruoloCorrente Il nuovo ruolo da assumere.
     * @throws IllegalArgumentException se il ruolo passato è null.
     */
    public void setRuoloCorrente(Ruolo ruoloCorrente) {
        if (ruoloCorrente == null) {
            throw new IllegalArgumentException("Il ruolo non può essere null.");
        }
        this.ruoloCorrente = ruoloCorrente;
        System.out.println("[Sessione] Cambio ruolo effettuato: " + ruoloCorrente);
    }
}