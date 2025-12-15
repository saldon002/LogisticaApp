package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

public class ClienteController {

    @FXML private TextField txtCodice;
    @FXML private ListView<String> listStorico;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();
    }

    @FXML
    void onCerca(ActionEvent event) {
        String codice = txtCodice.getText().trim();
        if (codice.isEmpty()) {
            mostraErrore("Attenzione", "Inserisci un codice valido.");
            return;
        }

        // Recupera lo storico tramite il Facade
        // Nota: se il codice non esiste, potremmo ricevere una lista vuota o null
        try {
            List<String> storico = facade.getStoricoSpedizione(codice);

            listStorico.getItems().clear();
            if (storico == null || storico.isEmpty()) {
                listStorico.getItems().add("Nessuna informazione trovata per il codice: " + codice);
            } else {
                listStorico.getItems().addAll(storico);
            }
        } catch (Exception e) {
            mostraErrore("Errore Database", "Impossibile recuperare i dati.");
            e.printStackTrace();
        }
    }

    @FXML
    void onLogout(ActionEvent event) throws IOException {
        App.setRoot("view/login");
    }

    private void mostraErrore(String titolo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}