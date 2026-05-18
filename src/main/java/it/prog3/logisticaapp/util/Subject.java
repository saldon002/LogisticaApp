package it.prog3.logisticaapp.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe base astratta per il pattern Observer.
 * <p>
 *     Gestisce la lista degli osservatori e la notifica.
 * </p>
 */
public abstract class Subject {

    // Lista degli osservatori iscritti final per evetire = null
    private final List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        // Scorre la lista e notifica tutti
        for (Observer observer : observers) {
            observer.update();
        }
    }
}