package it.prog3.logisticaapp;

import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("=== INIZIO TEST BACKEND ===");

        try {
            // 1. Istanziamo il Facade (simula l'avvio della GUI)
            System.out.println("1. Inizializzazione Facade...");
            LogisticaFacade facade = new LogisticaFacade();

            // --- SIMULAZIONE LOGIN (FIX PER SECURITY EXCEPTION) ---
            System.out.println("-> Simulo Login come MANAGER...");
            Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.MANAGER);
            // ------------------------------------------------------

            // 2. Stampiamo la flotta vuota (o come è nel DB prima del carico)
            System.out.println("\n2. Controllo Flotta Iniziale:");
            for (IVeicolo v : facade.getFlottaAttuale()) {
                System.out.println("   - " + v);
            }

            // 3. Simuliamo il click sul bottone "Carica Merce"
            System.out.println("\n3. Esecuzione Algoritmo di Carico...");
            facade.caricaMerce();

            // 4. Verifichiamo il risultato in memoria
            System.out.println("\n4. Risultato Post-Carico:");
            for (IVeicolo v : facade.getFlottaAttuale()) {
                System.out.println("   - " + v.toString());
                System.out.println("     Contenuto: " + v.getCarico().size() + " colli.");
                for(ICollo c : v.getCarico()) {
                    System.out.println("       -> " + c.getCodice() + " (" + c.getPeso() + "kg)");
                }
            }

            // 5. Test Ricerca e Proxy
            System.out.println("\n5. Test Ricerca Proxy:");
            // Sostituisci "C001" con un codice che sai esistere nel tuo DB
            String codiceTest = "C_01";
            ICollo c = facade.cercaCollo(codiceTest);
            if (c != null) {
                System.out.println("   Trovato: " + c.getCodice() + " | Stato: " + c.getStato());
                // Qui scatta il lazy loading se chiediamo lo storico
                // System.out.println("   Storico: " + c.getStorico()); 
            } else {
                System.out.println("   Collo " + codiceTest + " non trovato (normale se il DB è vuoto/diverso)");
            }

        } catch (Exception e) {
            System.err.println("\n!!! ERRORE CRITICO DURANTE IL TEST !!!");
            e.printStackTrace();
        }

        System.out.println("\n=== FINE TEST ===");
    }
}