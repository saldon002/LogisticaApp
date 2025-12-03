package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.List;

/**
 * Context per il pattern Strategy.
 * Mantiene un riferimento alla strategia corrente e permette di cambiarla a runtime.
 */
public class PackingContext {
    private PackingStrategy strategy;

    /**
     * Costruttore.
     * @param strategy La strategia iniziale da utilizzare.
     */
    public PackingContext(PackingStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Cambia la strategia dinamicamente.
     * @param strategy La nuova strategia.
     */
    public void setStrategy(PackingStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Esegue la logica incapsulata nella strategia corrente.
     */
    public void esegui(List<ICollo> colli, List<IVeicolo> flotta) {
        if (strategy == null) {
            throw new IllegalStateException("Nessuna strategia impostata!");
        }
        strategy.eseguiPacking(colli, flotta);
    }
}
