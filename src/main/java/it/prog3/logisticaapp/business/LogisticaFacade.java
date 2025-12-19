package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade (Service Layer).
 * Gestisce la logica "Multi-Azienda" e mantiene lo stato della sessione.
 */
public class LogisticaFacade {

    private final GestoreDatabase gestoreDatabase;
    private final PackingContext packingContext;

    // CACHE IN MEMORIA: Mantiene i veicoli e il loro carico corrente.
    // Fondamentale perché il DB non memorizza l'associazione Veicolo-Collo.
    private List<Azienda> elencoAziende;

    public LogisticaFacade() {
        this.gestoreDatabase = new GestoreDatabase();
        this.packingContext = new PackingContext(new NextFitStrategy());

        // Carichiamo la flotta UNA SOLA VOLTA all'avvio del Facade.
        // Da ora in poi lavoreremo su questi oggetti in memoria.
        this.elencoAziende = gestoreDatabase.getFlottaAll();
    }

    public void setStrategy(PackingStrategy strategy) {
        this.packingContext.setStrategy(strategy);
    }

    // =========================================================================
    // METODI PER IL MANAGER (GUI)
    // =========================================================================

    /**
     * Restituisce la lista delle aziende mantenuta in memoria.
     * NON ricarica dal DB per evitare di perdere le associazioni dei colli appena caricati.
     */
    public List<Azienda> getAziendeAll() {
        if (this.elencoAziende == null) {
            this.elencoAziende = gestoreDatabase.getFlottaAll();
        }
        return this.elencoAziende;
    }

    /**
     * Recupera i colli in attesa di spedizione.
     * Questo DEVE essere letto dal DB per vedere i nuovi arrivi o cambi di stato.
     */
    public List<ICollo> getColliInAttesa() {
        return gestoreDatabase.getColliInPreparazione();
    }

    /**
     * Esegue il caricamento lavorando sugli oggetti in memoria e salvando solo gli stati su DB.
     */
    public void eseguiCarico() {
        System.out.println("[Facade] Avvio procedura di carico...");

        // 1. Recupero Colli (Dal DB)
        List<ICollo> colliDaSpedire = gestoreDatabase.getColliInPreparazione();

        // 2. Recupero Flotta (Dalla Memoria)
        // NOTA: Non ricarichiamo dal DB qui, usiamo la flotta che abbiamo già in RAM
        // per accumulare il carico (se volessimo fare più carichi successivi).
        if (this.elencoAziende == null || this.elencoAziende.isEmpty()) {
            this.elencoAziende = gestoreDatabase.getFlottaAll();
        }

        if (colliDaSpedire.isEmpty()) {
            throw new IllegalStateException("Nessun collo da spedire in magazzino.");
        }

        // 3. Creazione "Flotta Globale" (Lista piatta per la Strategy)
        List<IVeicolo> flottaGlobale = new ArrayList<>();
        for (Azienda az : this.elencoAziende) {
            flottaGlobale.addAll(az.getFlotta());
        }

        if (flottaGlobale.isEmpty()) {
            throw new IllegalStateException("Nessun veicolo disponibile in nessuna azienda.");
        }

        // 4. Esecuzione Strategy (Riempie i veicoli IN MEMORIA)
        packingContext.esegui(colliDaSpedire, flottaGlobale);

        // 5. Salvataggio Persistente: Ora salviamo anche DOVE sono finiti i pacchi
        int colliCaricati = 0;

        // Iteriamo sui veicoli, perché sono loro che ora sanno quali pacchi hanno
        for (IVeicolo v : flottaGlobale) {
            for (ICollo c : v.getCarico()) {
                // Se il collo è stato appena caricato (o riconfermiamo che è lì)
                if ("CARICATO".equals(c.getStato()) || "IN_PREPARAZIONE".equals(c.getStato())) {

                    // Aggiorniamo stato in memoria per sicurezza
                    c.setStato("CARICATO");

                    // Salviamo nel DB l'associazione: Pacco C01 -> Veicolo V01
                    gestoreDatabase.associaColloVeicolo(c, v.getCodice());

                    colliCaricati++;
                }
            }
        }

        System.out.println("[Facade] Procedura terminata. Colli caricati: " + colliCaricati);

        // A questo punto:
        // - I colli su DB hanno stato "CARICATO" (quindi spariranno dalla tabella).
        // - I veicoli in 'this.elencoAziende' hanno la lista 'carico' piena.
        // - La GUI chiamerà getTutteLeAziende() e vedrà i veicoli pieni.
    }

    // =========================================================================
    // METODI DI SUPPORTO
    // =========================================================================

    public ICollo cercaCollo(String codice) {
        return gestoreDatabase.getColloProxy(codice);
    }

    public void aggiornaStatoCollo(ICollo c, String nuovoStato) {
        try {
            c.setStato(nuovoStato);
            gestoreDatabase.salvaCollo(c);
        } catch (SecurityException e) {
            throw e;
        }
    }

    public List<String> getStoricoCollo(String codice) {
        ColloReale c = gestoreDatabase.getColloRealeCompleto(codice);
        return (c != null) ? c.getStorico() : new ArrayList<>();
    }
}