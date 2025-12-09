package it.prog3.logisticaapp.business;

/**
 * Gestisce la sessione utente corrente tramite il pattern Singleton.
 * <p>
 * Questa classe permette di simulare il login di diversi attori (Manager, Corriere, Cliente)
 * senza dover implementare un sistema di autenticazione complesso.
 * Viene utilizzata dai Proxy per i controlli di sicurezza (Protection Proxy)
 * e dalla GUI per abilitare/disabilitare funzionalità.
 * </p>
 */

public class Sessione {
    private static Sessione instance;
    public enum Ruolo {
        MANAGER,
        CORRIERE,
        CLIENTE
    }
    private Ruolo ruoloCorrente;

    private Sessione(){
        this.ruoloCorrente = Ruolo.CLIENTE;
        System.out.println("[Sessione] Inizializzata con ruolo default: CLIENTE");
    }

    /**
     * Restituisce l'istanza unica della Sessione
     * @return L'oggetto Sessione
     */
    public static Sessione getInstance(){
        if(instance==null){
            instance = new Sessione();
        }
        return instance;
    }

    /**
     * Restituisce il ruolo dell'utente corrente
     * @return Il valore dell'Enum Ruolo
     */
    public Ruolo getRuoloCorrente() {
        return ruoloCorrente;
    }

    /**
     * Permette di cambiare il ruolo corrente (Simulazione Login)
     * @param ruoloCorrente Il nuovo ruolo da assumere
     */
    public void setRuoloCorrente(Ruolo ruoloCorrente) {
        this.ruoloCorrente = ruoloCorrente;
        System.out.println("[Sessione] Cambio ruolo effettuato con successo: " + ruoloCorrente);
    }
}
