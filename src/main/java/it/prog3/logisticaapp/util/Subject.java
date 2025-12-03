package it.prog3.logisticaapp.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta il Soggetto (Observable) nel pattern Observer.
 * <p>
 * Gestisce una lista di osservatori e fornisce i metodi per
 * registrarli (attach), rimuoverli (detach) e notificarli (notify).
 * </p>
 */
public class Subject {
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Aggiunge un observer alla lista
     * @param o L'observer da registrare
     * @throws IllegalArgumentException se l'osservatore è null
     */
    public void attach(Observer o) {
        if (o == null){
            throw new IllegalArgumentException("L'observer non può essere null");
        }
        if (!observers.contains(o)){
            observers.add(o);
        }
    }

    /**
     * Rimuove un observer dalla lista
     * @param o L'observer da rimuovere
     */
    public void detach(Observer o) {
        observers.remove(o);
    }

    /**
     * Notifica tutti gli observers registrati chiamando il loro metodo update()
     */
    public void notifyObservers() {
        for (Observer o : observers){
            o.update();
        }
    }
}
