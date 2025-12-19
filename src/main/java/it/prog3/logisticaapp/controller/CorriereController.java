package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.IVeicolo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

/*
public class CorriereController {

    @FXML private ComboBox<IVeicolo> comboVeicoli;
    @FXML private TextField txtLuogo;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();

        // Carichiamo la flotta attuale per permettere al corriere di scegliere il suo mezzo
        try {
            List<IVeicolo> flotta = facade.getFlottaAttuale();
            if (flotta.isEmpty()) {
                mostraAlert(Alert.AlertType.WARNING, "Attenzione", "Nessun veicolo trovato. Il manager ha caricato la merce?");
            } else {
                comboVeicoli.getItems().addAll(flotta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onRegistraTappa(ActionEvent event) {
        IVeicolo veicoloSelezionato = comboVeicoli.getValue();
        String luogo = txtLuogo.getText().trim();

        if (veicoloSelezionato == null) {
            mostraAlert(Alert.AlertType.ERROR, "Errore", "Devi selezionare un veicolo!");
            return;
        }
        if (luogo.isEmpty()) {
            mostraAlert(Alert.AlertType.ERROR, "Errore", "Devi inserire il luogo attuale!");
            return;
        }

        try {
            // Chiamata al Facade che aggiorna tutti i colli nel veicolo
            facade.registraTappaVeicolo(veicoloSelezionato.getCodice(), luogo);

            mostraAlert(Alert.AlertType.INFORMATION, "Successo",
                    "Tappa registrata a '" + luogo + "'.\nTracking aggiornato per tutti i colli a bordo.");

            txtLuogo.clear(); // Pulisci il campo per la prossima tappa

        } catch (IllegalArgumentException e) {
            mostraAlert(Alert.AlertType.WARNING, "Attenzione", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostraAlert(Alert.AlertType.ERROR, "Errore Critico", "Impossibile aggiornare i dati: " + e.getMessage());
        }
    }

    @FXML
    void onLogout(ActionEvent event) throws IOException {
        App.setRoot("view/login");
    }

    private void mostraAlert(Alert.AlertType type, String titolo, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
*/
