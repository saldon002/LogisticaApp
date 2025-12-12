package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import java.util.List;

/**
 * Facade (o Service Layer) che nasconde la complessità del sistema al Controller.
 * <p>
 * Coordina:
 * 1. Model: {@link Azienda} (mantiene lo stato in memoria).
 * 2. Database: {@link GestoreDatabase} (persistenza).
 * 3. Business Logic: {@link PackingContext} (algoritmo).
 * </p>
 */
public class LogisticaFacade {

    // L'istanza dell'azienda mantiene lo STATO della simulazione corrente (i veicoli e il loro carico).
    private final Azienda azienda;

    private final PackingContext packingContext;
    private final GestoreDatabase gestoreDatabase;

    public LogisticaFacade() {
        // 1. Inizializziamo il DB
        this.gestoreDatabase = new GestoreDatabase();

        // 2. Inizializziamo l'Azienda (Model)
        // Creiamo l'oggetto che conterrà la flotta in memoria.
        // Possiamo hardcodare "DHL" o prenderlo da una configurazione.
        this.azienda = new AziendaConcreta();
        this.azienda.setNome("DHL");

        // 3. Inizializziamo la Strategy (Default: NextFit)
        this.packingContext = new PackingContext(new NextFitStrategy());
    }

    /**
     * Esegue l'intero flusso di caricamento merce.
     * Pattern: Facade + Strategy.
     */
    public void caricaMerce() {
        System.out.println("[Service] Inizio procedura di carico...");

        // A. Recuperiamo i dati dal DB
        // Nota: Assicurati che GestoreDatabase abbia getColliInPreparazione() (come scritto prima)
        List<ICollo> colliInMagazzino = gestoreDatabase.getColliInPreparazione();

        // Recuperiamo i veicoli vuoti dal DB
        List<IVeicolo> flottaDalDB = gestoreDatabase.getFlotta(azienda.getNome());

        if (colliInMagazzino.isEmpty()) {
            System.out.println("[Service] Nessun collo da spedire.");
            return;
        }

        // B. Popoliamo il Model (Azienda)
        // È FONDAMENTALE salvare la flotta dentro l'oggetto Azienda per mantenere lo stato in memoria.
        this.azienda.setFlotta(flottaDalDB);

        // C. Eseguiamo l'algoritmo (Lavora sugli oggetti in memoria)
        packingContext.esegui(colliInMagazzino, this.azienda.getFlotta());

        // D. Salviamo le modifiche (Aggiorniamo lo stato dei colli a "CARICATO" nel DB)
        for (ICollo c : colliInMagazzino) {
            // Salviamo solo se l'algoritmo lo ha effettivamente caricato (stato cambiato)
            if ("CARICATO".equals(c.getStato())) {
                gestoreDatabase.salvaCollo(c);
            }
        }

        System.out.println("[Service] Procedura completata. Veicoli caricati: " + this.azienda.getFlotta().size());
    }

    /**
     * Cerca un collo tramite codice.
     * Usa il Proxy internamente (gestito da GestoreDatabase).
     */
    public ICollo cercaCollo(String codice) {
        return gestoreDatabase.getCollo(codice);
    }

    /**
     * Aggiorna lo stato di un collo (es. Spedito, Consegnato).
     * @param c Il collo (Proxy o Reale).
     * @param nuovoStato Il nuovo stato.
     */
    public void aggiornaStato(ICollo c, String nuovoStato) {
        try {
            // 1. Modifica logica (questo scatena il Proxy Protection e l'Observer!)
            c.setStato(nuovoStato);

            // 2. Persistenza: Se il proxy non ha lanciato eccezioni, salviamo nel DB.
            gestoreDatabase.salvaCollo(c);

        } catch (SecurityException e) {
            // Rilanciamo l'eccezione per farla gestire alla GUI (mostrare alert rosso)
            throw e;
        }
    }

    /**
     * Restituisce la flotta ATTUALE in memoria.
     * <p>
     * ATTENZIONE: Non richiama il DB! Restituisce l'oggetto Azienda che contiene
     * i veicoli con i pacchi appena caricati dall'algoritmo.
     * </p>
     */
    public List<IVeicolo> getFlottaAttuale() {
        // Se la flotta è vuota (appena aperto il programma), proviamo a caricarla vuota dal DB
        if (this.azienda.getFlotta() == null || this.azienda.getFlotta().isEmpty()) {
            List<IVeicolo> f = gestoreDatabase.getFlotta(azienda.getNome());
            this.azienda.setFlotta(f);
        }
        return this.azienda.getFlotta();
    }

    // Metodo extra per il reset (opzionale)
    public void resetSimulazione() {
        this.azienda.getFlotta().clear();
        // Ricarica pulita
        getFlottaAttuale();
    }
}