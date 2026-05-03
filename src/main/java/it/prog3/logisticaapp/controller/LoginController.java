package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.Sessione;
import javafx.fxml.FXML;
import java.io.IOException;

public class LoginController {

    /**
     * Accesso Area MANAGER (Admin).
     * Imposta il ruolo MANAGER nella sessione.
     */
    @FXML
    private void onOpenManager() throws IOException {
        // Impostiamo il ruolo corretto
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.MANAGER);
        System.out.println("[Login] Ruolo impostato: MANAGER");

        // Cambio scena
        App.setRoot("manager");
    }

    /**
     * Accesso Area CORRIERE.
     * Imposta il ruolo CORRIERE nella sessione.
     */
    @FXML
    private void onOpenCorriere() throws IOException {
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.CORRIERE);
        System.out.println("[Login] Ruolo impostato: CORRIERE");

        App.setRoot("corriere");
    }

    /**
     * Accesso Area CLIENTE (Tracking).
     * Imposta il ruolo CLIENTE nella sessione.
     */
    @FXML
    private void onOpenCliente() throws IOException {
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.CLIENTE);
        System.out.println("[Login] Ruolo impostato: CLIENTE");

        App.setRoot("cliente");
    }
}