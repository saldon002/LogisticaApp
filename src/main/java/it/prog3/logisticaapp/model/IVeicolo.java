package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il comportamento di un Veicolo generico.
 * <p>
 * Estende {@link Serializable} per la compatibilità con i requisiti JDBC/JavaBeans.
 * Rappresenta il ruolo "Product" nel pattern Factory Method.
 * </p>
 */
public interface IVeicolo extends Serializable {
    String getCodice();
    void setCodice(String codice);

    int getCapienza();
    void setCapienza(int capienza);

    String getAzienda();
    void setAzienda(String azienda);

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
