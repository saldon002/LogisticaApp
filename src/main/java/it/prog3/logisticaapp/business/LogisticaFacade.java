package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import it.prog3.logisticaapp.util.FileLogger;
import it.prog3.logisticaapp.util.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade Pattern.
 * Fornisce un'interfaccia semplificata per la logica di business.
 */
public class LogisticaFacade {

    private final GestoreDatabase gestoreDatabase;
    private final PackingContext packingContext;

    private Map<String, ICollo> cacheColli;
    private List<Azienda> elencoAziendeCache;

    public LogisticaFacade() {
        this.gestoreDatabase = new GestoreDatabase();
        this.packingContext = new PackingContext(new NextFitStrategy());
        this.cacheColli = new HashMap<>();
    }

    public void setStrategy(PackingStrategy strategy) {
        this.packingContext.setStrategy(strategy);
    }

    /**
     * Metodo Helper per la gestione della Cache.
     * Se l'oggetto è già in memoria, restituisce quello vecchio.
     * Se è nuovo, lo salva in memoria e lo restituisce.
     */
    private ICollo gestisciCache(ICollo c) {
        if (c == null) return null;

        // 1. Controllo se lo conosciamo già
        if (cacheColli.containsKey(c.getCodice())) {
            // Restituisco l'istanza che è già monitorata dal sistema
            return cacheColli.get(c.getCodice());
        }

        // 2. Se è nuovo, lo salvo
        cacheColli.put(c.getCodice(), c);
        new FileLogger(c);
        return c;
    }

    // =========================================================================
    // SEZIONE 1: MANAGER (Carico Merce)
    // =========================================================================

    public List<Azienda> getAziendeAll() {
        // Se non abbiamo la cache aziende, carichiamola dal DB
        if (this.elencoAziendeCache == null) {
            this.elencoAziendeCache = gestoreDatabase.getFlottaAll();

            for (Azienda a : elencoAziendeCache) {
                for (IVeicolo v : a.getFlotta()) {
                    for (ICollo c : v.getCarico()) {
                        gestisciCache(c); // Salviamo in cache
                    }
                }
            }
        }
        return this.elencoAziendeCache;
    }

    public List<ICollo> getColliInAttesa() {
        // Recuperiamo i dati grezzi dal DB
        List<ICollo> listDalDB = gestoreDatabase.getColliInPreparazione();
        List<ICollo> listMerge = new ArrayList<>();

        // Per ogni collo, verifichiamo se esiste già in cache
        for (ICollo c : listDalDB) {
            listMerge.add(gestisciCache(c));
        }
        return listMerge;
    }

    /**
     * Esegue il caricamento (Strategy), cambia stato e salva.
     */
    public void eseguiCarico() {
        System.out.println("[Facade] Avvio procedura di carico...");

        List<ICollo> colli = getColliInAttesa();
        if (colli.isEmpty()) throw new IllegalStateException("Nessun collo da spedire in magazzino.");

        List<IVeicolo> flottaGlobale = new ArrayList<>();
        for (Azienda az : getAziendeAll()) {
            flottaGlobale.addAll(az.getFlotta());
        }

        if (flottaGlobale.isEmpty()) throw new IllegalStateException("Nessun veicolo disponibile.");

        // Esecuzione Algoritmo
        packingContext.esegui(colli, flottaGlobale);

        // Salvataggio su DB
        salvaRisultatiCarico(getAziendeAll());
    }

    private void salvaRisultatiCarico(List<Azienda> aziende) {
        int spediti = 0;
        for (Azienda az : aziende) {
            for (IVeicolo v : az.getFlotta()) {
                boolean isPieno = (v.getCarico().size() == v.getCapienza());

                for (ICollo c : v.getCarico()) {
                    ICollo cached = gestisciCache(c);

                    if (isPieno) {
                        if (!"IN_TRANSITO".equals(cached.getStato())) {
                            cached.setStato("IN_TRANSITO");
                            gestoreDatabase.associaColloVeicolo(cached, v.getCodice());
                            gestoreDatabase.aggiornaTracking(cached.getCodice(), "Spedito con " + az.getNome());
                            spediti++;
                        }
                    } else {
                        // Se non parte, resta CARICATO
                        cached.setStato("CARICATO");
                        gestoreDatabase.associaColloVeicolo(cached, v.getCodice());
                    }
                }
            }
        }
        System.out.println("[Facade] Carico completato. Colli spediti: " + spediti);
    }

    // =========================================================================
    // SEZIONE 2: CORRIERE (Tracking)
    // =========================================================================

    public List<IVeicolo> getFlotta() {
        List<Azienda> aziende = getAziendeAll();
        List<IVeicolo> veicoliInViaggio = new ArrayList<>();

        for (Azienda az : aziende) {
            for (IVeicolo v : az.getFlotta()) {
                // Il corriere vede solo i veicoli pieni/partiti
                if (!v.getCarico().isEmpty()) {
                    veicoliInViaggio.add(v);
                }
            }
        }
        return veicoliInViaggio;
    }

    public void registraTappaVeicolo(IVeicolo veicolo, String luogo) {
        if (veicolo == null || veicolo.getCarico().isEmpty()) return;

        System.out.println("[Facade] Tappa a " + luogo);

        for (ICollo c : veicolo.getCarico()) {
            // Recuperiamo l'istanza corretta dalla cache
            ICollo cached = gestisciCache(c);

            if ("IN_TRANSITO".equals(cached.getStato())) {
                String msg = "Arrivato al centro smistamento: " + luogo;

                // 1. Aggiorna DB
                gestoreDatabase.aggiornaTracking(cached.getCodice(), msg);

                // 2. Aggiorna Oggetto in Memoria -> Scatta OBSERVER (Cliente vede aggiornamento)
                cached.aggiungiEventoStorico(msg);
            }
        }
    }

    // =========================================================================
    // SEZIONE 3: CLIENTE (Ricerca collo)
    // =========================================================================

    public ICollo cercaCollo(String codice) {
        // 1. Prima cerco nella cache (Priorità alla memoria)
        if (cacheColli.containsKey(codice)) {
            System.out.println("[Facade] Collo trovato in cache (Observer attivo).");
            return cacheColli.get(codice);
        }

        // 2. Se non c'è, carico dal DB
        System.out.println("[Facade] Collo non in cache, carico dal DB.");
        ICollo nuovo = gestoreDatabase.getColloProxy(codice);

        // 3. Lo metto in cache e lo restituisco
        return gestisciCache(nuovo);
    }

    public List<String> getStoricoCollo(String codice) {
        // Se è in cache, prendiamo lo storico in memoria (più aggiornato)
        if (cacheColli.containsKey(codice)) {
            return cacheColli.get(codice).getStorico();
        }
        // Altrimenti query DB
        ColloReale c = gestoreDatabase.getColloRealeCompleto(codice);
        return (c != null) ? c.getStorico() : new ArrayList<>();
    }
}