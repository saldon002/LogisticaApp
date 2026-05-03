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

    // Lista degli osservatori iscritti
    private final List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("DEBUG: Attach eseguito");
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