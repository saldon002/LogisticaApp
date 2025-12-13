package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta Creator del pattern Factory Method.
 * <p>
 * Rappresenta l'astrazione di un'azienda di logistica.
 * Segue il principio Open-Closed (OCP): la logica di gestione della flotta è chiusa,
 * ma la creazione dei veicoli è aperta all'estensione tramite sottoclassi.
 * </p>
 */
public abstract class Azienda {

    // Lista della flotta (DIP: Dipende dall'astrazione IVeicolo)
    protected List<IVeicolo> flotta;

    private String nome;

    public Azienda() {
        this.flotta = new ArrayList<>();
    }

    public Azienda(String nome) {
        this();
        setNome(nome);
    }

    /**
     * Metodo che utilizza il Factory Method.
     * <p>
     * Questo metodo è il "Client" del Factory Method interno.
     * Incapsula la logica di aggiunta alla lista, garantendo che ogni veicolo creato
     * venga gestito correttamente.
     * </p>
     *
     * @param tipo    Il tipo di veicolo da creare (es. "CAMION").
     * @param codice  Il codice del nuovo veicolo.
     * @throws IllegalArgumentException se il tipo di veicolo non è supportato.
     */
    public void nuovoVeicolo(String tipo, String codice) {
        // 1. Delegiamo la creazione alla sottoclasse (Factory Method)
        IVeicolo v = createVeicolo(tipo, codice);

        // 2. Controllo robustezza: Se la factory fallisce, dobbiamo avvisare il chiamante!
        if (v == null) {
            throw new IllegalArgumentException("Tipo veicolo non supportato: " + tipo);
        }

        // 3. Aggiungiamo alla flotta
        flotta.add(v);
        System.out.println("[Azienda " + nome + "] Aggiunto veicolo: " + v);
    }

    /**
     * Il Factory Method astratto (Hook).
     * Le sottoclassi concrete (es. AziendaConcreta) implementeranno lo switch di creazione.
     *
     * @return Un'istanza di IVeicolo, o null se il tipo non è gestito.
     */
    protected abstract IVeicolo createVeicolo(String tipo, String codice);

    // --- Getter e Setter ---

    public List<IVeicolo> getFlotta() {
        return flotta;
    }

    /**
     * Imposta la flotta (utile per il caricamento dal DB).
     */
    public void setFlotta(List<IVeicolo> flotta) {
        if (flotta == null) {
            this.flotta = new ArrayList<>(); // Defensive coding
        } else {
            this.flotta = flotta;
        }
    }

    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'azienda non può essere vuoto.");
        }
        this.nome = nome;
    }
}