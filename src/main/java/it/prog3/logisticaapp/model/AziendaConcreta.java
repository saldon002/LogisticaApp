package it.prog3.logisticaapp.model;

/**
 * Concrete Creator che implementa il Factory Method.
 */
public class AziendaConcreta extends Azienda {

    public AziendaConcreta(String nome) {
        super(nome);
    }

    @Override
    public IVeicolo createVeicolo(String tipo, String codice) {
        if (tipo == null) return null;

        if (tipo.equalsIgnoreCase("CAMION")) {
            return new Camion(codice);
        }
        else if (tipo.equalsIgnoreCase("FURGONE")) {
            return new Furgone(codice);
        }

        // Se il tipo non è riconosciuto, restituisce null.
        // La classe base Azienda lancerà l'eccezione appropriata.
        return null;
    }
}