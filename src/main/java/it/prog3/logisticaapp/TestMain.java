package it.prog3.logisticaapp;

import it.prog3.logisticaapp.database.DbSetup;
import it.prog3.logisticaapp.database.GestoreDatabase;
import it.prog3.logisticaapp.model.*;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("=== INIZIO TEST BACKEND ===");

        // 1. Reset e Creazione Tabelle
        DbSetup.main(args);

        GestoreDatabase dao = new GestoreDatabase();
        //dao.resetTabelle();

        // 2. Creazione AZIENDE e VEICOLI
        Azienda dhl = new AziendaConcreta("DHL");
        dhl.aggiungiVeicolo("CAMION", "V01");
        dhl.aggiungiVeicolo("CAMION", "V02");
        dhl.aggiungiVeicolo("CAMION", "V03");
        dhl.aggiungiVeicolo("FURGONE", "V04");
        dhl.aggiungiVeicolo("FURGONE", "V05");

        Azienda brt = new AziendaConcreta("BRT");
        brt.aggiungiVeicolo("CAMION", "V06");
        brt.aggiungiVeicolo("CAMION", "V07");
        brt.aggiungiVeicolo("FURGONE", "V08");
        brt.aggiungiVeicolo("FURGONE", "V09");
        brt.aggiungiVeicolo("FURGONE", "V10");

        // 3. Salvataggio FLOTTA su DB
        dao.inserisciAzienda(dhl);
        dao.inserisciAzienda(brt);

        // 4. Creazione COLLI
        for (int i = 1; i <= 25; i++) {
            String codice = String.format("C%02d", i);
            ICollo c = new ColloReale(codice, 1.0, "X", "Y");
            dao.inserisciCollo(c);
        }

        System.out.println("=== FINE TEST BACKEND ===");
    }
}