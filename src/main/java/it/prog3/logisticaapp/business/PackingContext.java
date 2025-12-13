package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.List;

/**
 * Classe Context del pattern Strategy.
 * <p>
 * Mantiene un riferimento a una strategia concreta (IPackingStrategy)
 * e delega ad essa l'esecuzione dell'algoritmo.
 * Questo permette di cambiare algoritmo a runtime (es. tramite setStrategy).
 * </p>
 */
public class PackingContext {

    private PackingStrategy strategy;

    /**
     * Costruttore che richiede una strategia iniziale.
     * @param strategy La strategia di default (es. NextFit).
     */
    public PackingContext(PackingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La strategia non può essere null");
        }
        this.strategy = strategy;
    }

    /**
     * Permette di cambiare strategia a runtime.
     * @param strategy La nuova strategia da utilizzare.
     */
    public void setStrategy(PackingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La strategia non può essere null");
        }
        this.strategy = strategy;
        System.out.println("[Strategy] Algoritmo cambiato in: " + strategy.getClass().getSimpleName());
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
