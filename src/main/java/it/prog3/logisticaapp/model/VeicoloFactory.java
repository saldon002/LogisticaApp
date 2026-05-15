package it.prog3.logisticaapp.model;

/**
 * Creator Astratto (Pattern Factory Method).
 * Definisce l'interfaccia per la creazione di un veicolo.
 */
public abstract class VeicoloFactory {
    public abstract IVeicolo createVeicolo(String codice);
}
