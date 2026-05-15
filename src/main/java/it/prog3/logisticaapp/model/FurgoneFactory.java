package it.prog3.logisticaapp.model;

/**
 * Concrete Creator per i Furgoni.
 */
public class FurgoneFactory extends VeicoloFactory {
    @Override
    public IVeicolo createVeicolo(String codice) {
        return new Furgone(codice);
    }
}