package it.prog3.logisticaapp.model;

/**
 * Concrete Creator che implementa il Factory Method.
 * Decide quale istanza creare basandosi sulla stringa "tipo".
 */
public class AziendaLogistica extends AziendaTrasporti {

    @Override
    public IVeicolo createVeicolo(String tipo, String codice, String azienda) {
        // Logica parametrica come da slide
        if (tipo == null) return null;

        if (tipo.equalsIgnoreCase("CAMION")) {
            return new Camion(codice, azienda);
        }
        else if (tipo.equalsIgnoreCase("FURGONE")) {
            return new Furgone(codice, azienda);
        }

        return null; // Tipo sconosciuto
    }
}
