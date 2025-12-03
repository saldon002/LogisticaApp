package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.Iterator;
import java.util.List;

/**
 * Implementazione concreta dell'algoritmo Next Fit.
 * <p>
 * Questo algoritmo scorre la lista dei colli e tenta di inserirli nel veicolo corrente.
 * Se il veicolo è pieno, passa al successivo e non torna più indietro.
 * </p>
 */
public class NextFitStrategy implements PackingStrategy {
    @Override
    public void eseguiPacking(List<ICollo> colli, List<IVeicolo> flotta) {
        if (colli == null || colli.isEmpty() || flotta == null || flotta.isEmpty()) {
            System.out.println("[NextFit] Nessun collo o nessun veicolo disponibile.");
            return;
        }

        System.out.println("[NextFit] Avvio algoritmo su " + colli.size() + " colli e " + flotta.size() + " veicoli.");

        // Iteratore per scorrere la flotta veicolo per veicolo
        Iterator<IVeicolo> veicoloIterator = flotta.iterator();

        // Prendiamo il primo veicolo disponibile
        IVeicolo veicoloCorrente = veicoloIterator.next();

        for (ICollo collo : colli) {

            // Ignoriamo i colli già spediti o consegnati
            if (!"IN_MAGAZZINO".equalsIgnoreCase(collo.getStato())) {
                continue;
            }

            // Proviamo a caricare sul veicolo corrente
            boolean caricato = veicoloCorrente.carica(collo);

            if (caricato) {
                // Se è entrato, aggiorniamo lo stato del collo
                collo.setStato("CARICATO");
                System.out.println("Collo " + collo.getCodice() + " caricato su " + veicoloCorrente.getCodice());
            } else {
                // Se NON è entrato, dobbiamo cambiare veicolo (Next Fit non controlla i precedenti)
                if (veicoloIterator.hasNext()) {
                    System.out.println("Veicolo " + veicoloCorrente.getCodice() + " pieno. Passo al prossimo.");

                    // Switch al prossimo veicolo
                    veicoloCorrente = veicoloIterator.next();

                    // Riprovo a caricare lo stesso collo sul nuovo veicolo
                    boolean caricatoSuNuovo = veicoloCorrente.carica(collo);
                    if (caricatoSuNuovo) {
                        collo.setStato("CARICATO");
                        System.out.println("Collo " + collo.getCodice() + " caricato su " + veicoloCorrente.getCodice());
                    } else {
                        System.err.println("Collo " + collo.getCodice() + " non entra nemmeno nel nuovo veicolo vuoto (troppo grande?)");
                    }
                } else {
                    // Veicoli finiti!
                    System.err.println("Flotta esaurita! Il collo " + collo.getCodice() + " è rimasto a terra.");
                }
            }
        }
        System.out.println("[NextFit] Algoritmo terminato.");
    }
}
