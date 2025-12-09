package it.prog3.logisticaapp.util;

/**
 * Rappresenta l'astrazione dell'Osservatore nel pattern Observer.
 * <p>
 * Le classi che estendono Observer (come i Controller) devono implementare
 * il metodo {@code update()} per reagire ai cambiamenti di stato del Subject.
 * </p>
 */
public abstract class Observer {

    /**
     * Riferimento al soggetto osservato.
     * È 'protected' per essere accessibile dalle sottoclassi concrete (es. MainController).
     */
    protected Subject subject;

    /**
     * Metodo chiamato dal Subject quando il suo stato cambia.
     * Deve essere implementato dalle classi concrete.
     */
    public abstract void update();

    // Getter e Setter opzionali, utili se vuoi cambiare soggetto a runtime
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}