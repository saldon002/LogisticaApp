package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.Sessione;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import java.io.IOException;

public class LoginController {

    @FXML private ComboBox<Sessione.Ruolo> comboRuolo;

    @FXML
    public void initialize() {
        // Popola la tendina con i ruoli
        comboRuolo.setItems(FXCollections.observableArrayList(Sessione.Ruolo.values()));
    }

    @FXML
    public void onAccediClick() throws IOException {
        Sessione.Ruolo ruolo = comboRuolo.getValue();

        if (ruolo == null) {
            mostraErrore("Seleziona un ruolo per continuare.");
            return;
        }

        // 1. Impostiamo la sessione globale
        Sessione.getInstance().setRuoloCorrente(ruolo);

        // 2. ROUTING: Decidiamo quale file FXML caricare in base al ruolo
        if (ruolo == Sessione.Ruolo.CLIENTE) {
            // Carica interfaccia Cliente
            App.setRoot("view/cliente");
        } else {
            // Carica interfaccia Staff (Manager o Corriere)
            App.setRoot("view/staff");
        }
    }

    private void mostraErrore(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}