package it.prog3.logisticaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta Creator del pattern Factory Method.
 * <p>
 * Rappresenta l'astrazione di un'azienda di logistica.
 * </p>
 */
public abstract class Azienda {

    private String nome;
    private List<IVeicolo> flotta;

    public Azienda() {
        setFlotta(new ArrayList<>());
    }

    public Azienda(String nome) {
        this();
        setNome(nome);
    }

    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'azienda non può essere vuoto.");
        }
        this.nome = nome;
    }

    public List<IVeicolo> getFlotta() {
        return flotta;
    }

    public void setFlotta(List<IVeicolo> flotta) {
        this.flotta = (flotta != null) ? flotta : new ArrayList<>();
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
    public void aggiungiVeicolo(String tipo, String codice) {
        // 1. Deleghiamo la creazione alla sottoclasse (Factory Method)
        IVeicolo v = createVeicolo(tipo, codice);

        // 2. Controllo
        if (v == null) {
            throw new IllegalArgumentException("Tipo veicolo non supportato: " + tipo);
        }

        // 3. Aggiungiamo alla flotta
        flotta.add(v);
        System.out.println("[Azienda " + nome + "] Creato e aggiunto veicolo: " + v);
    }

    /**
     * Metodo per aggiungere un veicolo già istanziato.
     */
    public void aggiungiVeicoloEsistente(IVeicolo v) {
        if (v != null) {
            flotta.add(v);
        }
    }

    /**
     * Il Factory Method astratto.
     *
     * @return Un'istanza di IVeicolo, o null se il tipo non è gestito.
     */
    public abstract IVeicolo createVeicolo(String tipo, String codice);

    @Override
    public String toString() {
        return nome + " (" + flotta.size() + " veicoli)";
    }

}