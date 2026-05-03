package it.prog3.logisticaapp.util;

/**
 * Interfaccia Observer (parte del pattern Observer).
 * <p>
 * Definisce il contratto per gli oggetti che vogliono essere notificati
 * dei cambiamenti di stato in un oggetto Subject.
 * </p>
 */
public interface Observer {

    //Metodo chiamato dal Subject quando il suo stato cambia.
    void update();
}