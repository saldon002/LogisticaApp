package it.prog3.logisticaapp.model;

/**
 * Concrete Creator per i Camion.
 */
public class CamionFactory extends VeicoloFactory {
    @Override
    public IVeicolo createVeicolo(String codice) {
        return new Camion(codice);
    }
}