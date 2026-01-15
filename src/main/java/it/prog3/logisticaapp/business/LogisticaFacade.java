package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import it.prog3.logisticaapp.util.FileLogger;
import it.prog3.logisticaapp.util.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade Pattern.
 * Fornisce un'interfaccia semplificata per la logica di business complessa.
 * Nasconde la complessità delle interazioni tra Database, Strategy e Model.
 */
public class LogisticaFacade {

    private final GestoreDatabase gestoreDatabase;
    private final PackingContext packingContext;

    // Cache semplice per mantenere lo stato della flotta durante la sessione Manager
    private List<Azienda> elencoAziendeCache;

    public LogisticaFacade() {
        // Iniezione delle dipendenze (Hardcoded per semplicità, ma idealmente iniettate)
        this.gestoreDatabase = new GestoreDatabase();
        // Default Strategy: Next Fit
        this.packingContext = new PackingContext(new NextFitStrategy());
    }

    // =========================================================================
    // SEZIONE MANAGER (Carico Merce)
    // =========================================================================

    public List<Azienda> getAziendeAll() {
        if (this.elencoAziendeCache == null) {
            this.elencoAziendeCache = gestoreDatabase.getFlottaAll();
        }
        return this.elencoAziendeCache;
    }

    public List<ICollo> getColliInAttesa() {
        return gestoreDatabase.getColliInPreparazione();
    }

    /**
     * Cuore della Business Logic:
     * 1. Recupera i dati.
     * 2. Esegue l'algoritmo di Bin Packing (Strategy).
     * 3. Persiste i cambiamenti sul DB.
     */
    public void eseguiCarico() {
        System.out.println("[Facade] Inizio procedura di carico...");

        // 1. Recupero Colli
        List<ICollo> colli = getColliInAttesa();
        if (colli.isEmpty()) throw new IllegalStateException("Nessun collo da spedire.");

        // 2. Recupero Flotta (Flattening: Da Lista Aziende a Lista Veicoli)
        List<IVeicolo> flottaGlobale = new ArrayList<>();
        for (Azienda az : getAziendeAll()) {
            flottaGlobale.addAll(az.getFlotta());
        }
        if (flottaGlobale.isEmpty()) throw new IllegalStateException("Nessun veicolo disponibile.");

        // 3. Esecuzione Strategy (Modifica gli oggetti in memoria)
        packingContext.esegui(colli, flottaGlobale);

        // 4. Salvataggio risultati (Refactoring: Metodo estratto per pulizia)
        salvaRisultatiCarico(getAziendeAll());
    }

    private void salvaRisultatiCarico(List<Azienda> aziende) {
        int spediti = 0;
        for (Azienda az : aziende) {
            for (IVeicolo v : az.getFlotta()) {
                // Logica di business: Un veicolo parte solo se è PIENO (policy rigorosa)
                // Oppure potremmo dire "se ha almeno 1 collo". Qui manteniamo la tua logica "pieno".
                boolean veicoloPronto = (v.getCarico().size() == v.getCapienza());

                for (ICollo c : v.getCarico()) {
                    // Colleghiamo il Logger per tracciare questo evento
                    attachLogger(c);

                    if (veicoloPronto) {
                        if (!"IN_TRANSITO".equals(c.getStato())) {
                            c.setStato("IN_TRANSITO");
                            gestoreDatabase.associaColloVeicolo(c, v.getCodice());
                            gestoreDatabase.aggiornaTracking(c.getCodice(), "Partito con " + az.getNome());
                            spediti++;
                        }
                    } else {
                        // Veicolo non pieno: Il pacco è caricato ma il camion non parte.
                        c.setStato("CARICATO");
                        gestoreDatabase.associaColloVeicolo(c, v.getCodice());
                    }
                }
            }
        }
        System.out.println("[Facade] Carico completato. Colli spediti: " + spediti);
    }

    // =========================================================================
    // SEZIONE CORRIERE (Tracking)
    // =========================================================================

    public List<IVeicolo> getFlottaViaggiante() {
        List<IVeicolo> inViaggio = new ArrayList<>();
        // Ricarichiamo fresco dal DB per il corriere
        List<Azienda> aziende = gestoreDatabase.getFlottaAll();

        for (Azienda az : aziende) {
            for (IVeicolo v : az.getFlotta()) {
                // Filtro: Il corriere vede solo i camion con merce a bordo
                if (!v.getCarico().isEmpty()) {
                    inViaggio.add(v);
                }
            }
        }
        return inViaggio;
    }

    public void registraTappaVeicolo(IVeicolo veicolo, String luogo) {
        if (veicolo == null || veicolo.getCarico().isEmpty()) return;

        System.out.println("[Facade] Tappa a " + luogo + " per veicolo " + veicolo.getCodice());

        for (ICollo c : veicolo.getCarico()) {
            attachLogger(c);

            // Aggiorniamo solo i colli effettivamente in viaggio
            if ("IN_TRANSITO".equals(c.getStato())) {
                String msg = "Arrivato a centro: " + luogo;

                // Aggiorna DB (Storico)
                gestoreDatabase.aggiornaTracking(c.getCodice(), msg);

                // Aggiorna Oggetto in memoria (Observer notifica Logger)
                c.aggiungiEventoStorico(msg);
            }
        }
    }

    // =========================================================================
    // UTILS
    // =========================================================================

    public void setStrategy(PackingStrategy s) {
        this.packingContext.setStrategy(s);
    }

    // Observer Helper
    private void attachLogger(ICollo c) {
        if (c instanceof Subject) {
            // Nota: Poiché creiamo i proxy a nuovo, non c'è rischio duplicati in questa sessione.
            ((Subject) c).attach(new FileLogger(c));
        }
    }
}