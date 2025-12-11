package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il comportamento di un Veicolo generico.
 * <p>
 * Estende {@link Serializable} per la compatibilità con JDBC.
 * Rappresenta il ruolo "Product" nel pattern Factory Method.
 * Non contiene riferimenti all'Azienda (gestita dal Creator).
 * </p>
 */
public interface IVeicolo extends Serializable {

    // --- Identificativi (Traccia: Codice e Tipo) ---

    String getCodice();
    void setCodice(String codice);

    /**
     * Restituisce il tipo di veicolo (es. "CAMION", "FURGONE").
     * Utile per la visualizzazione in Tabella e per il rispetto della traccia.
     * @return Stringa descrittiva del tipo.
     */
    String getTipo();

    // --- Capacità (Traccia: Capienza container) ---

    int getCapienza();
    void setCapienza(int capienza);

    // --- Logica di Business ---

    /**
     * Tenta di caricare un collo sul veicolo.
     * @param collo Il collo da aggiungere.
     * @return true se il caricamento ha successo, false se non c'è spazio.
     */
    boolean carica(ICollo collo);

    /**
     * Restituisce la lista dei colli attualmente caricati.
     * @return Lista di ICollo.
     */
    List<ICollo> getCarico();
}