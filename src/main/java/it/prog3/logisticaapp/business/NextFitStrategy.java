package it.prog3.logisticaapp.business;

import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import java.util.Iterator;
import java.util.List;

/**
 * Concrete Strategy: Implementazione dell'algoritmo Next Fit.
 * <p>
 * LOGICA NEXT FIT:
 * Mantiene un riferimento all'ultimo veicolo utilizzato.
 * Se un collo entra nel veicolo corrente, lo aggiunge.
 * Se non entra, chiude il veicolo corrente, passa al successivo nella lista e prova ad aggiungerlo lì.
 * Non controlla mai i veicoli precedenti.
 * </p>
 */
public class NextFitStrategy implements PackingStrategy { // Usa IPackingStrategy

    @Override
    public void eseguiPacking(List<ICollo> colli, List<IVeicolo> flotta) {
        if (colli == null || colli.isEmpty() || flotta == null || flotta.isEmpty()) {
            System.out.println("[NextFit] Dati insufficienti per avviare l'algoritmo.");
            return;
        }

        System.out.println("[NextFit] Avvio su " + colli.size() + " colli e " + flotta.size() + " veicoli.");

        // Usiamo l'iteratore per gestire lo scorrimento progressivo della flotta
        Iterator<IVeicolo> veicoloIterator = flotta.iterator();

        // Iniziamo dal primo veicolo
        IVeicolo veicoloCorrente = veicoloIterator.next();

        for (ICollo collo : colli) {

            // 1. Controllo Stato: Deve corrispondere a quello nel DB (GestoreDatabase)
            // Se nel DB usi "IN_PREPARAZIONE", devi usare quella stringa qui.
            if (!"IN_PREPARAZIONE".equalsIgnoreCase(collo.getStato())) {
                continue; // Ignora colli già processati
            }

            // 2. Prova inserimento nel veicolo corrente
            boolean caricato = veicoloCorrente.caricaCollo(collo);

            if (caricato) {
                // Successo! Aggiorna stato e logga
                collo.setStato("CARICATO");
                System.out.println(" -> Collo " + collo.getCodice() + " caricato su " + veicoloCorrente.getCodice());
            } else {
                // Fallimento: Il veicolo è pieno (o il collo è troppo grande).
                // NextFit dice: passa al prossimo e non guardare indietro.

                if (veicoloIterator.hasNext()) {
                    System.out.println(" -> Veicolo " + veicoloCorrente.getCodice() + " pieno/inadatto. Cambio veicolo.");

                    // Switch al prossimo
                    veicoloCorrente = veicoloIterator.next();

                    // Riprova sul nuovo veicolo (appena aperto, quindi vuoto)
                    if (veicoloCorrente.caricaCollo(collo)) {
                        collo.setStato("CARICATO");
                        System.out.println(" -> Collo " + collo.getCodice() + " caricato su NUOVO veicolo " + veicoloCorrente.getCodice());
                    } else {
                        // Se non entra nemmeno in un veicolo vuoto, il collo è fisicamente troppo grande
                        System.err.println(" -> ERRORE: Il collo " + collo.getCodice() + " è troppo grande/pesante per il veicolo " + veicoloCorrente.getTipo());
                    }
                } else {
                    // Non ci sono più veicoli nella flotta
                    System.err.println(" -> FLOTTA ESAURITA! Impossibile caricare collo " + collo.getCodice());
                    // Col NextFit, se finiscono i veicoli, solitamente ci si ferma o si lasciano a terra i restanti.
                }
            }
        }
        System.out.println("[NextFit] Procedura terminata.");
    }
}