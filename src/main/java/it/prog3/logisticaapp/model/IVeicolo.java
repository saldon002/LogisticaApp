package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per un Veicolo generico.
 * <p>
 * Rappresenta il ruolo "Product" nel pattern Factory Method.
 * Estende {@link Serializable} per la compatibilità con il passaggio dati e JDBC.
 * </p>
 */
public interface IVeicolo extends Serializable {

    // --- Identificativi ---

    String getCodice();

    /**
     * Imposta il codice identificativo del veicolo.
     * @param codice Il codice (non null, non vuoto).
     * @throws IllegalArgumentException se il codice non è valido.
     */
    void setCodice(String codice);

    /**
     * Restituisce il tipo di veicolo (es. "CAMION", "FURGONE").
     * <p>
     * <b>Nota OCP:</b> Usare questo valore SOLO per visualizzazione (GUI) o persistenza (DB).
     * Non usarlo per logica condizionale (if tipo == "CAMION").
     * </p>
     * @return Stringa descrittiva del tipo.
     */
    String getTipo();

    // --- Capacità ---

    int getCapienza();

    /**
     * Imposta la capienza massima del veicolo.
     * @param capienza La capienza (deve essere > 0).
     * @throws IllegalArgumentException se la capienza è <= 0.
     */
    void setCapienza(int capienza);

    // --- Logica di Business ---

    /**
     * Tenta di caricare un collo sul veicolo.
     * Implementa la logica di controllo spazio.
     *
     * @param collo Il collo da aggiungere.
     * @return true se il caricamento ha successo (c'è spazio), false altrimenti.
     * @throws IllegalArgumentException se il collo è null.
     */
    boolean carica(ICollo collo);

    /**
     * Restituisce la lista (copia o riferimento) dei colli attualmente caricati.
     * @return Lista di ICollo.
     */
    List<ICollo> getCarico();
}