package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta Creator del pattern Factory Method.
 * Corrisponde alla classe "Application" delle slide.
 * Gestisce la lista dei veicoli (Flotta) e definisce il metodo di creazione.
 */
public abstract class Azienda {

    // Lista della flotta (Aggregazione)
    // Protected per permettere accesso alle sottoclassi se necessario
    protected List<IVeicolo> flotta = new ArrayList<>();

    // Nome dell'azienda (utile per identificarla nel DB o nella GUI)
    private String nome;

    public Azienda() {
        // Costruttore vuoto per JavaBean
        this.flotta =  new ArrayList<>();
    }

    public Azienda(String nome) {
        this();
        this.nome = nome;
    }

    /**
     * Metodo Template che usa il Factory Method.
     * Crea un veicolo e lo aggiunge automaticamente alla flotta di QUESTA azienda.
     *
     * @param tipo    Il tipo di veicolo da creare (es. "CAMION").
     * @param codice  Il codice del nuovo veicolo.
     */
    public void nuovoVeicolo(String tipo, String codice) {
        // 1. Chiama il metodo astratto (Factory Method)
        // NOTA: Non passiamo più 'azienda' perché il veicolo è agnostico.
        IVeicolo v = createVeicolo(tipo, codice);

        // 2. Aggiunge il prodotto alla lista di questa istanza
        if (v != null) {
            flotta.add(v);
            System.out.println("Veicolo " + v.getCodice() + " aggiunto alla flotta di " + this.nome);
        } else {
            System.err.println("Impossibile creare veicolo di tipo: " + tipo);
        }
    }

    /**
     * Il Factory Method astratto.
     * Le sottoclassi implementeranno lo switch.
     */
    protected abstract IVeicolo createVeicolo(String tipo, String codice);

    // --- Getter e Setter ---

    public List<IVeicolo> getFlotta() {
        return flotta;
    }

    public void setFlotta(List<IVeicolo> flotta) {
        this.flotta = flotta;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}