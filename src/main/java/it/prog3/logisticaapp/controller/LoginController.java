package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.Sessione;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class LoginController {

    @FXML
    void onLoginManager(ActionEvent event) throws IOException {
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.MANAGER);
        App.setRoot("view/manager");
    }

    @FXML
    void onLoginCorriere(ActionEvent event) throws IOException {
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.CORRIERE);
        // Ora reindirizziamo alla view specifica del corriere
        App.setRoot("view/corriere");
    }

    @FXML
    void onLoginCliente(ActionEvent event) throws IOException {
        Sessione.getInstance().setRuoloCorrente(Sessione.Ruolo.CLIENTE);
        // Reindirizziamo alla schermata di tracking
        App.setRoot("view/cliente");
    }
}