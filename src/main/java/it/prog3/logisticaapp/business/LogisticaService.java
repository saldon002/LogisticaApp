package it.prog3.logisticaapp.business;

//import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import java.util.List;

/**
 * Facade che nasconde la complessità del sistema al Controller.
 * Coordina Model (Azienda), Database (GestoreDatabase) e Business Logic (PackingContext).
 */
public class LogisticaService {
    private final Azienda azienda;
    private final PackingContext packingContext;
    private final GestoreDatabase gestoreDatabase;

    public LogisticaService() {
        // 1. Inizializziamo il DB (Facade pattern: nascondiamo il DB al controller)
        this.gestoreDatabase = new GestoreDatabase();

        // 2. Inizializziamo l'Azienda (Factory Method: usiamo l'implementazione concreta)
        this.azienda = new AziendaConcreta();

        // 3. Inizializziamo la Strategy (Default: NextFit)
        this.packingContext = new PackingContext(new NextFitStrategy());
    }

    /**
     * Metodo principale del Facade: esegue l'intero flusso di caricamento.
     * 1. Recupera i dati dal DB.
     * 2. Esegue l'algoritmo.
     * 3. Salva i risultati.
     */
    public void caricaMerce() {
        System.out.println("[Service] Inizio procedura di carico...");

        // A. Recuperiamo i dati dal DB (simuliamo il metodo, lo scriveremo nel prossimo step)
        List<ICollo> colliInMagazzino = gestoreDatabase.getColliInMagazzino();
        List<IVeicolo> flottaDisponibile = gestoreDatabase.getFlotta("DHL"); // Esempio azienda

        // B. Popoliamo l'azienda (Model)
        // Nota: AziendaConcreta eredita setFlotta da Azienda (se l'abbiamo messo, altrimenti usiamo il loop)
        // Per semplicità, assumiamo di poter passare la flotta o di doverla creare.
        // Se ArchivioDati restituisce già IVeicolo creati, li usiamo.

        // C. Eseguiamo l'algoritmo
        packingContext.esegui(colliInMagazzino, flottaDisponibile);

        // D. Salviamo le modifiche (Stato colli e Carico veicoli)
        for (ICollo c : colliInMagazzino) {
            gestoreDatabase.salvaCollo(c); // Aggiorna stato a "CARICATO"
        }

        // Salviamo anche l'associazione veicolo-colli se il DB lo richiede
        // gestoreDatabase.aggiornaVeicoli(flottaDisponibile);

        System.out.println("[Service] Procedura completata.");
    }

    /**
     * Cerca un collo tramite codice.
     * Usa il Proxy internamente (gestito da ArchivioDati).
     */
    public ICollo cercaCollo(String codice) {
        return gestoreDatabase.getCollo(codice);
    }

    /**
     * Aggiorna lo stato di un collo.
     * @param c Il collo (Proxy o Reale).
     * @param nuovoStato Il nuovo stato.
     */
    public void aggiornaStato(ICollo c, String nuovoStato) {
        // 1. Modifica logica (questo scatena il Proxy Protection e l'Observer!)
        c.setStato(nuovoStato);

        // 2. Persistenza
        gestoreDatabase.salvaCollo(c);
    }

    // Getter di utilità per la GUI
    public List<IVeicolo> getFlottaAttuale() {
        // Qui dovremmo ritornare la flotta caricata in memoria o dal DB
        return gestoreDatabase.getFlotta("DHL");
    }
}
