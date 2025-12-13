package it.prog3.logisticaapp.util;

/**
 * Interfaccia per il pattern Observer.
 * <p>
 * Definisce il contratto per gli oggetti che vogliono essere notificati
 * dei cambiamenti di stato di un Subject.
 * Trasformata in interfaccia per permettere ai Controller JavaFX di implementarla
 * senza vincoli di ereditarietà singola.
 * </p>
 */
public interface Observer {

    /**
     * Metodo chiamato dal Subject quando il suo stato cambia.
     * Le classi concrete (es. MainController) definiranno qui la logica di reazione.
     */
    void update();
}