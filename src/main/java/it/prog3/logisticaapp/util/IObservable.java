package it.prog3.logisticaapp.util;

/**
 * Interfaccia segregata per il pattern Observer (ISP).
 * <p>
 *  Definisce solo i metodi necessari alla gestione degli osservatori.
 * </p>
 */
public interface IObservable {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}