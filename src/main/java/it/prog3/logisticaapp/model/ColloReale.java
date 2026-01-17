package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta l'oggetto Reale nel pattern Proxy.
 * <p>
 * Implementazione concreta di un collo che mantiene tutti i dati in memoria.
 * Estende {@link Subject} per ereditare la logica di notifica (Observer Pattern).
 * </p>
 */
public class ColloReale extends Subject implements ICollo {

    private String codice;
    private String stato;
    private String mittente;
    private String destinatario;
    private double peso;
    private List<String> storico;

    /**
     * Costruttore vuoto necessario per la serializzazione.
     * Inizializza le liste per evitare NullPointerException.
     */
    public ColloReale() {
        this.storico = new ArrayList<>();
        this.stato = "IN_PREPARAZIONE"; // Stato iniziale di default
    }

    /**
     * Costruttore con parametri.
     */
    public ColloReale(String codice, double peso, String mittente, String destinatario) {
        this();
        setCodice(codice);
        setPeso(peso);
        setMittente(mittente);
        setDestinatario(destinatario);
    }

    @Override
    public String getCodice() { return codice; }

    @Override
    public void setCodice(String codice) {
        if (codice == null || codice.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice del collo non può essere vuoto.");
        }
        this.codice = codice;
    }

    @Override
    public String getStato() { return stato; }

    @Override
    public void setStato(String stato) {
        this.stato = stato;
        notifyObservers();
        System.out.println("DEBUG: Cambio stato in corso...");
    }

    @Override
    public double getPeso() { return peso; }

    @Override
    public void setPeso(double peso) {
        if (peso <= 0) {
            throw new IllegalArgumentException("Il peso deve essere maggiore di zero.");
        }
        this.peso = peso;
    }

    @Override
    public String getMittente() { return mittente; }
    @Override
    public void setMittente(String mittente) { this.mittente = mittente; }

    @Override
    public String getDestinatario() { return destinatario; }
    @Override
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    @Override
    public void aggiungiEventoStorico(String evento) {
        this.storico.add(0, evento);
        notifyObservers();
    }

    @Override
    public List<String> getStorico() { return storico; }

    // Metodo per popolare lo storico, usato dal DB
    public void setStorico(List<String> storico) {
        // Se null, credo lista vuota
        this.storico = (storico != null) ? storico : new ArrayList<>();
    }

    @Override
    public String toString() {
        return codice + " (" + stato + ")";
    }
}