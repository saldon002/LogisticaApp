package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.ColloReale;

/**
 * Interfaccia per il caricamento dei dati (Dependency Inversion Principle).
 * <p>
 * Questa astrazione permette al Proxy di non dipendere direttamente dalla classe concreta GestoreDatabase.
 * </p>
 */
public interface IDataLoader {

    /**
     * Recupera l'oggetto reale completo (dati + storico).
     *
     * @param codice Il codice univoco del collo.
     * @return L'istanza di ColloReale popolata, o null se non trovato.
     */
    ColloReale getColloRealeCompleto(String codice);
}
