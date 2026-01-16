package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.List;

/**
 * Interfaccia del pattern Strategy per gli algoritmi di Bin Packing.
 * <p>
 * Definisce il metodo comune che tutti gli algoritmi dovranno implementare per distribuire i colli nei veicoli.
 * </p>
 */
public interface PackingStrategy {

    /**
     * Esegue l'algoritmo di riempimento.
     *
     * @param colli  La lista dei colli da caricare.
     * @param flotta La lista dei veicoli disponibili.
     */
    void eseguiPacking(List<ICollo> colli, List<IVeicolo> flotta);
}
