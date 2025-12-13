package it.prog3.logisticaapp.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe base astratta per il pattern Observer.
 * Implementa l'interfaccia IObservable rispettando il principio DRY (codice scritto una volta sola).
 */
public abstract class Subject implements IObservable {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer o) {
        if (o == null) throw new IllegalArgumentException("Observer nullo");
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}