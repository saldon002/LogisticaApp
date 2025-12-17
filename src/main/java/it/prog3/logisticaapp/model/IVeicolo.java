package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.IObservable;
import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per un Veicolo generico.
 * <p>
 * Rappresenta il ruolo "Product" nel pattern Factory Method.
 * Estende {@link it.prog3.logisticaapp.util.IObservable} per permettere alla GUI di aggiornarsi quando il carico cambia.
 * Estende {@link Serializable} per la compatibilità con il passaggio dati e JDBC.
 * </p>
 */
public interface IVeicolo extends IObservable, Serializable {

    String getCodice();
    void setCodice(String codice);
    String getTipo();
    int getCapienza();
    void setCapienza(int capienza);
    List<ICollo> getCarico();
    boolean caricaCollo(ICollo collo);
}