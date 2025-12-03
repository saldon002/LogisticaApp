package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione concreta dell'oggetto Collo.
 * <p>
 *     Questa classse rappresenta il "RealSubject" nel pattern Proxy e il "ConcreteSubject"
 *     nel pattern Observer. Contiene tutti i dati, incluso lo storico pesante.
 * </p>
 */
public class ColloReale extends Subject implements ICollo{
    private String codice;
    private String stato;
    private String mittente;
    private String destinatario;
    private double peso;
    private List<String> storico;

    /**
     * Costruttore vuoto richiesto dalle specifiche JavaBean
     */
    public ColloReale() {
        this.storico = new ArrayList<>();
    }

    /**
     * @param codice Identificativo univoco
     * @param peso Peso collo
     * @param mittente Nome mittente
     * @param destinatario Nome destinatario
     */
    public ColloReale(String codice, double peso, String mittente, String destinatario) {
        this(); // Chiama il costruttore vuoto per init liste
        this.codice = codice;
        this.peso = peso;
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.stato = "IN_PREPARAZIONE"; // Stato di default
    }

    // Implementazione Metodi ICollo
    @Override
    public String getCodice() {return codice;}
    @Override
    public void setCodice(String codice) {this.codice = codice;}
    @Override
    public String getStato() {return stato;}
    @Override
    public void setStato(String stato) {
        this.stato = stato;
        // Metodo ereditato da Subject: avvisa la GUI che lo stato è cambiato
        super.notifyObservers();
    }
    @Override
    public String getMittente() {return mittente;}
    @Override
    public void setMittente(String mittente) {this.mittente = mittente;}
    @Override
    public String getDestinatario() {return destinatario;}
    @Override
    public void setDestinatario(String destinatario)  {this.destinatario = destinatario;}
    @Override
    public double getPeso() {return peso;}
    @Override
    public void setPeso(double peso) {
        if (peso < 0) throw new IllegalArgumentException("Peso non valido.");
        this.peso = peso;
    }
    @Override
    public List<String> getStorico() {return storico;}
    //Metodo per popolare lo storico
    public void setStorico(List<String> storico) {this.storico = storico;}


    //DEBUG
    @Override
    public String toString() {
        return "Collo{" + "codice='" + codice + '\'' + ", stato='" + stato + '\'' + '}';
    }
}
