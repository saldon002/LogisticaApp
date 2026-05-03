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

    String getCodice();
    void setCodice(String codice);
    String getTipo();
    int getCapienza();
    void setCapienza(int capienza);
    List<ICollo> getCarico();
    boolean caricaCollo(ICollo collo);
}