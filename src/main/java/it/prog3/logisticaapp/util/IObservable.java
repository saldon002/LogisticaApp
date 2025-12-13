package it.prog3.logisticaapp.util;

/**
 * Interfaccia segregata per il pattern Observer (ISP).
 * Definisce solo i metodi necessari alla gestione degli osservatori.
 */
public interface IObservable {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}