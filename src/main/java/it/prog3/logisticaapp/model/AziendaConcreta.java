package it.prog3.logisticaapp.model;

/**
 * Concrete Creator che implementa il Factory Method.
 * <p>
 * Questa classe contiene la logica di istanziazione specifica.
 * È l'unico punto del codice che necessita modifiche se vengono aggiunti nuovi tipi di veicoli
 * (isolamento della violazione OCP).
 * </p>
 */
public class AziendaConcreta extends Azienda {

    public AziendaConcreta() {
        super();
    }

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