package it.prog3.logisticaapp.util;

import javax.security.auth.Subject;

/**
 * Rappresenta l'astrazione dell'Osservatore nel pattern Observer.
 * <p>
 * Le classi che estendono Observer devono implementare
 * il metodo {@code update()} per reagire ai cambiamenti di stato del Subject.
 * </p>
 */
public abstract class Observer {
    private Subject subject;
    public Subject getSubject() {
        return subject;
    }
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    public abstract void update();
}
