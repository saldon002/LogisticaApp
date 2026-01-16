package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.Iterator;
import java.util.List;

/**
 * Concrete Strategy: Implementazione dell'algoritmo Next Fit.
 * <p>
 * Algoritmo O(N): Scorre i colli e i veicoli una sola volta.
 * Mantiene aperto solo il veicolo corrente. Se il collo non entra,
 * chiude il veicolo e passa al successivo (senza backtracking).
 * </p>
 */
public class NextFitStrategy implements PackingStrategy {

    @Override
    public void eseguiPacking(List<ICollo> colli, List<IVeicolo> flotta) {
        // Controllo
        if (colli == null || colli.isEmpty()) {
            System.out.println("[NextFit] Nessun collo da processare.");
            return;
        }
        if (flotta == null || flotta.isEmpty()) {
            throw new IllegalStateException("Impossibile avviare il carico: Flotta vuota.");
        }

        System.out.println("[NextFit] Avvio algoritmo su " + colli.size() + " colli.");

        Iterator<IVeicolo> veicoloIterator = flotta.iterator();
        IVeicolo veicoloCorrente = veicoloIterator.next();

        for (ICollo collo : colli) {
            // Filtriamo solo i colli pronti
            if (!"IN_PREPARAZIONE".equalsIgnoreCase(collo.getStato())) {
                continue;
            }

            // Tenta inserimento nel veicolo corrente
            boolean inserito = veicoloCorrente.caricaCollo(collo);

            if (inserito) {
                markAsCaricato(collo, veicoloCorrente);
            } else {
                // NEXT FIT: Se non entra, chiudi e passa al prossimo
                if (veicoloIterator.hasNext()) {
                    veicoloCorrente = veicoloIterator.next();
                    // Riprova nel nuovo veicolo (vuoto)
                    if (veicoloCorrente.caricaCollo(collo)) {
                        markAsCaricato(collo, veicoloCorrente);
                    } else {
                        // Caso critico: Il collo è più grande della capienza totale del veicolo vuoto
                        System.err.println("ERRORE: Collo " + collo.getCodice() + " troppo grande per " + veicoloCorrente.getTipo());
                    }
                } else {
                    System.err.println("FLOTTA ESAURITA: Impossibile caricare collo " + collo.getCodice());
                    // Qui l'algoritmo si ferma o lascia a terra i restanti.
                }
            }
        }
    }

    private void markAsCaricato(ICollo c, IVeicolo v) {
        c.setStato("CARICATO");
        System.out.println(" -> [OK] Collo " + c.getCodice() + " >> Veicolo " + v.getCodice());
    }
}