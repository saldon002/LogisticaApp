package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Facade (Service Layer) che nasconde la complessità del sistema.
 * <p>
 * Agisce come unico punto di ingresso per il Controller.
 * Gestisce il flusso dati tra Database, Model (Azienda) e Business Logic (Strategy).
 * </p>
 */
public class LogisticaFacade {

    private final Azienda azienda;
    private final PackingContext packingContext; // Il contesto della Strategy
    private final GestoreDatabase gestoreDatabase;

    public LogisticaFacade() {
        this.gestoreDatabase = new GestoreDatabase();

        // Factory Method implicito nell'uso di AziendaConcreta
        this.azienda = new AziendaConcreta("DHL");

        // Strategy Default: NextFit
        this.packingContext = new PackingContext(new NextFitStrategy());
    }

    /**
     * Permette di cambiare l'algoritmo di caricamento a runtime.
     * (Esempio di flessibilità del pattern Strategy).
     */
    public void setStrategy(PackingStrategy strategy) {
        this.packingContext.setStrategy(strategy);
    }

    /**
     * Esegue l'intero flusso di caricamento:
     * 1. Recupero Dati (DB) -> 2. Elaborazione (Strategy) -> 3. Persistenza (DB)
     */
    public void caricaMerce() {
        System.out.println("[Facade] Inizio procedura di carico...");

        // 1. Recupero dati dal DB (Se fallisce, l'eccezione risale al Controller)
        List<ICollo> colliInMagazzino = gestoreDatabase.getColliInPreparazione();
        List<IVeicolo> flottaDalDB = gestoreDatabase.getFlotta(azienda.getNome());

        if (colliInMagazzino.isEmpty()) {
            throw new IllegalArgumentException("Nessun collo da spedire in magazzino!");
        }
        if (flottaDalDB.isEmpty()) {
            throw new IllegalArgumentException("Nessun veicolo disponibile per l'azienda " + azienda.getNome());
        }

        // 2. Aggiorno il Model in memoria
        this.azienda.setFlotta(flottaDalDB);

        // 3. Eseguo l'algoritmo (Lavora sugli oggetti Java in memoria)
        // L'algoritmo modificherà lo stato dei colli e riempirà i veicoli
        packingContext.esegui(colliInMagazzino, this.azienda.getFlotta());

        // 4. Salvo le modifiche nel DB
        int cont = 0;
        for (ICollo c : colliInMagazzino) {
            // Salviamo solo se lo stato è cambiato (Ottimizzazione)
            if (!"IN_PREPARAZIONE".equals(c.getStato())) {
                gestoreDatabase.salvaCollo(c);
                cont++;
            }
        }

        System.out.println("[Facade] Procedura completata. Colli caricati: " + cont);
    }

    /**
     * Metodo per la ricerca puntuale.
     */
    public ICollo cercaCollo(String codice) {
        return gestoreDatabase.getCollo(codice);
    }

    /**
     * Aggiorna lo stato di un collo gestendo sicurezza e persistenza.
     * Dimostra l'uso del Proxy e dell'Observer (notifica implicita).
     */
    public void aggiornaStato(ICollo c, String nuovoStato) {
        try {
            // 1. Modifica nel dominio (scatta il Protection Proxy)
            c.setStato(nuovoStato);
            // Nota: Se c è un ColloReale collegato a una GUI, qui parte la notifica Observer!

            // 2. Persistenza
            gestoreDatabase.salvaCollo(c);

        } catch (SecurityException e) {
            // Rilanciamo l'eccezione specifica di sicurezza
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento stato", e);
        }
    }

    /**
     * Restituisce la situazione attuale della flotta (post-algoritmo).
     */
    public List<IVeicolo> getFlottaAttuale() {
        // Lazy loading: se la lista è vuota, la carico dal DB
        if (this.azienda.getFlotta().isEmpty()) {
            List<IVeicolo> f = gestoreDatabase.getFlotta(azienda.getNome());
            this.azienda.setFlotta(f);
        }
        return this.azienda.getFlotta();
    }


    public List<ICollo> getTuttiIColliInMagazzino() {
        // Richiama il DAO per avere la lista grezza
        return gestoreDatabase.getColliInPreparazione();
    }



    public List<String> getStoricoSpedizione(String codice) {
        // Recupera l'oggetto reale completo (che contiene lo storico) tramite il DAO
        ColloReale c = gestoreDatabase.getColloRealeCompleto(codice);
        if (c != null && c.getStorico() != null) {
            return c.getStorico();
        }
        return new ArrayList<>(); // Ritorna lista vuota se non c'è storico
    }

    /**
     * Metodo "Bulk Update" per il Corriere.
     * Registra la tappa per TUTTI i colli presenti su un dato veicolo.
     */
    public void registraTappaVeicolo(String targaVeicolo, String luogo) {
        System.out.println("[Facade] Aggiornamento tappa per veicolo: " + targaVeicolo);

        // 1. Cerchiamo il veicolo nella flotta
        List<IVeicolo> flotta = getFlottaAttuale();
        IVeicolo veicoloTrovato = null;

        // Ciclo for classico (NO LAMBDA come richiesto)
        for (IVeicolo v : flotta) {
            if (v.getCodice().equals(targaVeicolo)) {
                veicoloTrovato = v;
                break;
            }
        }

        if (veicoloTrovato == null) {
            throw new IllegalArgumentException("Veicolo non trovato: " + targaVeicolo);
        }

        // 2. Iteriamo su tutti i colli del veicolo
        List<ICollo> carico = veicoloTrovato.getCarico();
        if (carico.isEmpty()) {
            throw new IllegalArgumentException("Il veicolo è vuoto!");
        }

        int aggiornati = 0;
        for (ICollo c : carico) {
            try {
                // A. Aggiorniamo lo stato (Scatta il Proxy Protection)
                c.setStato("IN_TRANSITO");

                // B. Aggiungiamo la riga allo storico
                c.aggiungiEventoStorico("Arrivato a: " + luogo);

                // C. Salviamo nel DB
                gestoreDatabase.salvaCollo(c);

                aggiornati++;
            } catch (SecurityException e) {
                // Se non hai i permessi, lancia l'errore e ferma tutto
                throw e;
            }
        }

        System.out.println("[Facade] Tappa registrata. Colli aggiornati: " + aggiornati);
    }
}