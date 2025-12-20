package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import it.prog3.logisticaapp.util.FileLogger;
import it.prog3.logisticaapp.util.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade (Service Layer).
 * Gestisce la logica del Manager (Carico) e del Corriere (Tappe/Tracking).
 */
public class LogisticaFacade {

    private final GestoreDatabase gestoreDatabase;
    private final PackingContext packingContext;

    // Cache per il Manager (mantiene i veicoli caricati in memoria durante la sessione)
    private List<Azienda> elencoAziende;

    public LogisticaFacade() {
        this.gestoreDatabase = new GestoreDatabase();
        this.packingContext = new PackingContext(new NextFitStrategy());

        // Caricamento iniziale per il Manager
        this.elencoAziende = gestoreDatabase.getFlottaAll();
    }

    public void setStrategy(PackingStrategy strategy) {
        this.packingContext.setStrategy(strategy);
    }

    // =========================================================================
    // SEZIONE 1: METODI MANAGER
    // =========================================================================

    public List<Azienda> getAziendeAll() {
        // Lazy loading / Refresh se null
        if (this.elencoAziende == null) {
            this.elencoAziende = gestoreDatabase.getFlottaAll();
        }
        return this.elencoAziende;
    }

    public List<ICollo> getColliInAttesa() {
        return gestoreDatabase.getColliInPreparazione();
    }

    /**
     * Esegue il caricamento (Strategy), cambia stato a IN_TRANSITO e inizializza lo storico.
     */
    public void eseguiCarico() {
        System.out.println("[Facade] Avvio procedura di carico...");

        // 1. Recupero Dati
        List<ICollo> colliDaSpedire = gestoreDatabase.getColliInPreparazione();

        if (this.elencoAziende == null || this.elencoAziende.isEmpty()) {
            this.elencoAziende = gestoreDatabase.getFlottaAll();
        }

        if (colliDaSpedire.isEmpty()) {
            throw new IllegalStateException("Nessun collo da spedire in magazzino.");
        }

        // 2. Creazione "Flotta Globale"
        List<IVeicolo> flottaGlobale = new ArrayList<>();
        for (Azienda az : this.elencoAziende) {
            flottaGlobale.addAll(az.getFlotta());
        }

        if (flottaGlobale.isEmpty()) {
            throw new IllegalStateException("Nessun veicolo disponibile.");
        }

        // 3. Esecuzione Strategy (Riempie i veicoli IN MEMORIA)
        packingContext.esegui(colliDaSpedire, flottaGlobale);

        // 4. Salvataggio su DB
        int colliProcessati = 0;

        for (Azienda az : this.elencoAziende) {
            for (IVeicolo v : az.getFlotta()) {

                // Controlliamo se il veicolo è pieno
                boolean isPieno = (v.getCarico().size() == v.getCapienza());

                // Iteriamo sui colli caricati (sia parziali che pieni)
                for (ICollo c : v.getCarico()) {

                    attachLogger(c);

                    if (isPieno) {
                        // CASO A: Veicolo Pieno -> SPEDIAMO
                        if (!"IN_TRANSITO".equals(c.getStato())) {
                            c.setStato("IN_TRANSITO");

                            // Salviamo associazione e stato
                            gestoreDatabase.associaColloVeicolo(c, v.getCodice());

                            // Tracking
                            String msg = "Spedito con " + az.getNome() + ". Il pacco ha lasciato la struttura del mittente.";
                            gestoreDatabase.aggiornaTracking(c.getCodice(), msg);

                            colliProcessati++;
                        }
                    } else {
                        // CASO B: Veicolo Parziale -> SALVIAMO SOLO POSIZIONE
                        // Il pacco resta "IN_PREPARAZIONE" ma ricordiamo che è su questo camion.
                        // Così se chiudi l'app, al riavvio lo ritrovi lì.

                        c.setStato("CARICATO"); // Ribadiamo lo stato
                        gestoreDatabase.associaColloVeicolo(c, v.getCodice());

                        // NON aggiungiamo tracking perché non è ancora partito
                    }
                }
            }
        }

        System.out.println("[Facade] Procedura terminata. Colli spediti: " + colliProcessati);
    }

    // =========================================================================
    // SEZIONE 2: METODI CORRIERE
    // =========================================================================

    /**
     * Recupera TUTTI i veicoli di TUTTE le aziende.
     * FILTRO: Il corriere vede SOLO i veicoli che sono PIENI (quindi effettivamente partiti).
     */
    public List<IVeicolo> getFlotta() {
        List<Azienda> tutteLeAziende = gestoreDatabase.getFlottaAll();
        List<IVeicolo> veicoliInViaggio = new ArrayList<>();

        for (Azienda az : tutteLeAziende) {
            for (IVeicolo v : az.getFlotta()) {
                // Il corriere deve vedere solo i veicoli pieni (quelli che il Manager ha "spedito")
                // Quelli parziali sono ancora fermi in magazzino.
                if (v.getCarico().size() == v.getCapienza() && !v.getCarico().isEmpty()) {
                    veicoliInViaggio.add(v);
                }
            }
        }

        return veicoliInViaggio;
    }

    /**
     * Registra la tappa (Rimane invariato, funziona su qualsiasi veicolo passato).
     */
    public void registraTappaVeicolo(IVeicolo veicolo, String luogo) {
        if (veicolo == null || veicolo.getCarico().isEmpty()) {
            throw new IllegalArgumentException("Veicolo vuoto o non valido.");
        }

        System.out.println("[Facade] Aggiornamento tappa per " + veicolo.getCodice() + " a " + luogo);

        int aggiornati = 0;

        for (ICollo c : veicolo.getCarico()) {
            attachLogger(c);
            if ("IN_TRANSITO".equals(c.getStato())) {
                String messaggio = "Arrivato a centro di smistamento: " + luogo;

                gestoreDatabase.aggiornaTracking(c.getCodice(), messaggio);

                try {
                    c.aggiungiEventoStorico(messaggio);
                } catch (Exception e) { /* Ignora */ }

                aggiornati++;
            }
        }
        veicolo.notifyObservers();
        System.out.println("[Facade] Tappa registrata per " + aggiornati + " colli.");
    }

    // =========================================================================
    // METODI DI SUPPORTO (CLIENTE/TRACKING)
    // =========================================================================

    public ICollo cercaCollo(String codice) {
        return gestoreDatabase.getColloProxy(codice);
    }

    public List<String> getStoricoCollo(String codice) {
        ColloReale c = gestoreDatabase.getColloRealeCompleto(codice);
        return (c != null) ? c.getStorico() : new ArrayList<>();
    }

    private void attachLogger(ICollo c) {
        if (c instanceof Subject) {
            Subject s = (Subject) c;

            // 2. Logger su File (per persistenza e audit storico)
            s.attach(new FileLogger(c));
        }
    }
}