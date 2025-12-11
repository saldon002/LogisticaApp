package it.prog3.logisticaapp.model;

/**
 * Concrete Creator che implementa il Factory Method.
 * Decide quale istanza creare basandosi sulla stringa "tipo".
 */
public class AziendaConcreta extends Azienda {

    @Override
    public IVeicolo createVeicolo(String tipo, String codice) {
        // Logica parametrica come da slide
        if (tipo == null) return null;

        if (tipo.equalsIgnoreCase("CAMION")) {
            return new Camion(codice);
        }
        else if (tipo.equalsIgnoreCase("FURGONE")) {
            return new Furgone(codice);
        }

        return null; // Tipo sconosciuto
    }
}
