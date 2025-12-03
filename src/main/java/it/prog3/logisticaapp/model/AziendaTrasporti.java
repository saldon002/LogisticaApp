package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta Creator del pattern Factory Method.
 * Corrisponde alla classe "Application" delle slide.
 */
public abstract class AziendaTrasporti {

    // Lista della flotta (Aggregazione)
    private List<IVeicolo> flotta = new ArrayList<>();

    /**
     * Metodo Template che usa il Factory Method.
     * Corrisponde a "NewDocument" delle slide: crea, aggiunge e (opzionale) apre.
     *
     * @param tipo Il tipo di veicolo da creare (es. "CAMION").
     * @param codice Il codice del nuovo veicolo.
     * @param azienda Il nome dell'azienda.
     */
    public void nuovoVeicolo(String tipo, String codice, String azienda) {
        // 1. Chiama il metodo astratto (Factory Method)
        IVeicolo v = createVeicolo(tipo, codice, azienda);

        // 2. Aggiunge il prodotto alla lista
        if (v != null) {
            flotta.add(v);
            System.out.println("Veicolo aggiunto alla flotta: " + v.getCodice());
        } else {
            System.err.println("Impossibile creare veicolo di tipo: " + tipo);
        }
    }

    /**
     * Il Factory Method astratto.
     * Le sottoclassi dovranno implementare lo switch per creare l'oggetto giusto.
     */
    protected abstract IVeicolo createVeicolo(String tipo, String codice, String azienda);

    // Getter per la flotta (necessario per passarla all'algoritmo)
    public List<IVeicolo> getFlotta() {
        return flotta;
    }

    // Setter per la flotta (utile se carichiamo da DB in blocco)
    public void setFlotta(List<IVeicolo> flotta) {
        this.flotta = flotta;
    }
}
