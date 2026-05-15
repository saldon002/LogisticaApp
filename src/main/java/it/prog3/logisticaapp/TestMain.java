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

        // 2. Creazione AZIENDE e VEICOLI
        VeicoloFactory creaCamion = new CamionFactory();
        VeicoloFactory creaFurgone = new FurgoneFactory();

        Azienda dhl = new Azienda("DHL");
        Azienda brt = new Azienda("BRT");

        dhl.aggiungiVeicolo(creaCamion.createVeicolo("V01"));
        dhl.aggiungiVeicolo(creaCamion.createVeicolo("V02"));
        dhl.aggiungiVeicolo(creaCamion.createVeicolo("V03"));
        dhl.aggiungiVeicolo(creaFurgone.createVeicolo("V04"));
        dhl.aggiungiVeicolo(creaFurgone.createVeicolo("V05"));
        brt.aggiungiVeicolo(creaCamion.createVeicolo("V06"));
        brt.aggiungiVeicolo(creaCamion.createVeicolo("V07"));
        brt.aggiungiVeicolo(creaFurgone.createVeicolo("V08"));
        brt.aggiungiVeicolo(creaFurgone.createVeicolo("V09"));
        brt.aggiungiVeicolo(creaFurgone.createVeicolo("V10"));

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